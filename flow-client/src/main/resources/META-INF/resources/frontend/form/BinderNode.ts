/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import {
  AbstractModel,
  binderNodeSymbol,
  getName,
  getValue,
  keySymbol,
  parentSymbol,
  setValue,
  validatorsSymbol
} from "./Models";
import {Validator, ValueError} from "./Validation";
import {Binder, binderNodesSymbol} from "./Binder";
import {BinderState} from "./BinderState";

const errorsSymbol = Symbol('errors');
const visitedSymbol = Symbol('visited');

export class BinderNode<T, M extends AbstractModel<T>> implements BinderState<T, M> {
  private [visitedSymbol]: boolean = false;
  private [errorsSymbol]: ReadonlyArray<ValueError<T>> = [];

  constructor(readonly model: M) {
    this.model = model;
    this.binder[binderNodesSymbol].add(this);
  }

  get parent(): BinderNode<any, AbstractModel<any>> {
    return this.model[parentSymbol][binderNodeSymbol];
  }

  get isRoot(): boolean {
    return this.parent === this;
  }

  get binder(): Binder<any, AbstractModel<any>> {
    const modelParent = this.model[parentSymbol];
    return (modelParent instanceof Binder) ? modelParent : this.parent.binder;
  }

  get name(): string {
    return getName(this.model);
  }

  get value(): T {
    return getValue(this.model);
  }

  set value(newValue: T) {
    setValue(this.model, newValue);
    if (this.visited || this.invalid) {
      this.validate();
    }
  }

  get defaultValue(): T {
    if (this.isRoot) {
      this.binder.defaultValue;
    }

    return this.parent.defaultValue[this.model[keySymbol]];
  }

  get dirty(): boolean {
    return this.value !== this.defaultValue;
  }

  get validators(): ReadonlyArray<Validator<T>> {
    return this.model[validatorsSymbol];
  }

  requestValidation(): ReadonlyArray<Promise<ValueError<any> | void>> {
    return this.validators.map(
      validator => this.binder.requestValidation(this.model, validator)
    );
  }

  private requestValidationWithParents(): ReadonlyArray<Promise<ValueError<any> | void>> {
    return ([
      ...this.requestValidation(),
      ...(this.isRoot ? [] : this.parent.requestValidationWithParents())
    ]);
  }

  delete() {
    this.binder[binderNodesSymbol].delete(this);
  }

  async validate(): Promise<void> {
    if (!this.visited) {
      return;
    }

    await Promise.all(this.requestValidationWithParents());
  }

  get visited() {
    return this[visitedSymbol];
  }

  set visited(v) {
    this[visitedSymbol] = v;
    this.validate();
  }

  get errors() {
    return this[errorsSymbol];
  }

  get invalid() {
    return this.errors.length > 0;
  }
}
