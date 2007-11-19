/* *************************************************************************
 
 IT Mill Toolkit 

 Development of Browser User Interfaces Made Easy

 Copyright (C) 2000-2006 IT Mill Ltd
 
 *************************************************************************

 This product is distributed under commercial license that can be found
 from the product package on license.pdf. Use of this product might 
 require purchasing a commercial license from IT Mill Ltd. For guidelines 
 on usage, see licensing-guidelines.html

 *************************************************************************
 
 For more information, contact:
 
 IT Mill Ltd                           phone: +358 2 4802 7180
 Ruukinkatu 2-4                        fax:   +358 2 4802 7181
 20540, Turku                          email:  info@itmill.com
 Finland                               company www: www.itmill.com
 
 Primary source for information and releases: www.itmill.com

 ********************************************************************** */

package com.itmill.toolkit.tests.featurebrowser;

import com.itmill.toolkit.ui.Component;
import com.itmill.toolkit.ui.Label;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.TabSheet;

public class FeatureTabSheet extends Feature {

    public FeatureTabSheet() {
        super();
    }

    protected Component getDemoComponent() {

        OrderedLayout l = new OrderedLayout();

        TabSheet ts = new TabSheet();
        ts
                .addTab(
                        new Label(
                                "This is an example Label component that is added into Tab 1."),
                        "Tab 1 caption", null);
        ts
                .addTab(
                        new Label(
                                "This is an example Label component that is added into Tab 2."),
                        "Tab 2 caption", null);
        ts
                .addTab(
                        new Label(
                                "This is an example Label component that is added into Tab 3."),
                        "Tab 3 caption", null);
        l.addComponent(ts);

        // Properties
        propertyPanel = new PropertyPanel(ts);

        setJavadocURL("ui/TabSheet.html");

        return l;
    }

    protected String getExampleSrc() {
        return "TabSheet ts = new TabSheet();\n"
                + "ts.addTab(new Label(\"This is an example Label component that is added into Tab 1.\"),\"Tab 1 caption\",null);\n"
                + "ts.addTab(new Label(\"This is an example Label component that is added into Tab 2.\"),\"Tab 2 caption\",null);\n"
                + "ts.addTab(new Label(\"This is an example Label component that is added into Tab 3.\"),\"Tab 3 caption\",null);";
    }

    protected String getDescriptionXHTML() {
        return "A multicomponent container with tabs for switching between them.<br/>"
                + "In the normal case, one would place a layout component on each tab.<br/><br />"
                + "On the demo tab you can try out how the different properties affect "
                + "the presentation of the component.";
    }

    protected String getImage() {
        return "icon_demo.png";
    }

    protected String getTitle() {
        return "TabSheet";
    }

}
