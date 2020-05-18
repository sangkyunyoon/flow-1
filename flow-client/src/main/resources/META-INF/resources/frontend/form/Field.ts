/* tslint:disable:max-classes-per-file */

import { directive, Part, PropertyPart } from "lit-html";
import {
  AbstractModel,
  binderStateSymbol,
  fromStringSymbol,
  getName,
  getValue,
  requiredSymbol,
  setValue
} from "./Models";
import { ValueError } from "./Validation";

// export const fieldSymbol = Symbol('field');

interface Field {
  required: boolean,
  invalid: boolean,
  errorMessage: string
}

export interface FieldStrategy extends Field {
  element: Element;
  validate: () => Promise<ReadonlyArray<ValueError<any>>>;
}

interface FieldState extends Field {
  name: string,
  value: string,
  visited: boolean
  strategy: FieldStrategy
}
const fieldStateMap = new WeakMap<PropertyPart, FieldState>();

class VaadinFieldStrategy implements FieldStrategy {
  constructor(public element: Element & Field) {}
  validate = async () => [];
  set required(value: boolean) { this.element.required = value }
  set invalid(value: boolean) { this.element.invalid = value }
  set errorMessage(value: string) { this.element.errorMessage = value }
}

class GenericFieldStrategy implements FieldStrategy {
  constructor(public element: Element) {}
  validate = async () => [];
  set required(value: boolean) { this.setAttribute('required', value) }
  set invalid(value: boolean) { this.setAttribute('invalid', value) }
  set errorMessage(_: string) { }
  setAttribute(key: string, val: any) {
    if (val) {
      this.element.setAttribute(key, '');
    } else {
      this.element.removeAttribute(key);
    }
  }
}

// vaadin elements have a `version` static property in the class
const isVaadinElement = (elm: Element) => (elm.constructor as any).version;

export const field = directive(<T>(
  model: AbstractModel<T>,
  effect?: (element: Element) => void
) => (part: Part) => {
  const propertyPart = part as PropertyPart;
  if (!(part instanceof PropertyPart) || propertyPart.committer.name !== '..') {
    throw new Error('Only supports ...="" syntax');
  }

  let fieldState: FieldState;
  const element = propertyPart.committer.element as HTMLInputElement & Field;

  let fieldStrategy: FieldStrategy;

  if (!fieldStateMap.has(propertyPart)) {
    fieldState = {
      name: '',
      value: '',
      required: false,
      invalid: false,
      errorMessage: '',
      visited: false,
      strategy: isVaadinElement(element) ? new VaadinFieldStrategy(element) : new GenericFieldStrategy(element)
    };

    fieldState.strategy.validate = async () => {
      fieldState.visited = true;

      const errors = await model[binderStateSymbol].validate();

      const displayedError = errors[0];
      fieldStrategy.invalid = fieldState.invalid = displayedError !== undefined;
      fieldStrategy.errorMessage = fieldState.errorMessage = displayedError?.validator.message || '';

      if (effect !== undefined) {
        effect.call(element, element);
      }
      return errors;
    };

    const updateValueFromElement = () => {
      fieldState.value = element.value;
      model[binderStateSymbol].visited = true;
      setValue(model, (model as any)[fromStringSymbol](element.value));
      if (effect !== undefined) {
        effect.call(element, element);
      }
    };

    element.oninput = () => {
      updateValueFromElement();
      if (fieldState.visited) {
        fieldStrategy.validate();
      }
    };

    element.onchange = element.onblur = () => {
      updateValueFromElement();
      fieldStrategy.validate();
    };

    element.checkValidity = () => !fieldState.invalid;
  } else {
    fieldState = fieldStateMap.get(propertyPart)!;
  }

  const name = getName(model);
  if (name !== fieldState.name) {
    fieldState.name = name;
    element.setAttribute('name', name);
  }

  const value = String(getValue(model));
  if (value !== fieldState.value) {
    fieldState.value = value;
    element.value = value;
  }

  const required = model[requiredSymbol];
  if (required !== fieldState.required) {
    fieldState.required = required;
    fieldState.strategy.required = required;
  }
});
