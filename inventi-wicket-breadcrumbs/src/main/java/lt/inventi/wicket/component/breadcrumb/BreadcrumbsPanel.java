package lt.inventi.wicket.component.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import lt.inventi.wicket.component.breadcrumb.collapse.DisplayedBreadcrumb;
import lt.inventi.wicket.component.breadcrumb.collapse.IBreadcrumbCollapser;
import lt.inventi.wicket.component.breadcrumb.hierarchy.IBreadcrumbHierarchy;

public class BreadcrumbsPanel extends GenericPanel<List<DisplayedBreadcrumb>> {
   
	private static final long serialVersionUID = 1L;
	private final ListView<DisplayedBreadcrumb> breadcrumbs;

    public BreadcrumbsPanel(String id) {
        super(id);

        add(breadcrumbs = new ListView<DisplayedBreadcrumb>("crumbs", new LoadableDetachableModel<List<DisplayedBreadcrumb>>() {
         
			private static final long serialVersionUID = 1L;

			@Override
            public List<DisplayedBreadcrumb> load() {
                IBreadcrumbCollapser collapser = BreadcrumbsSettings.getBreadcrumbsCollapser();
                IBreadcrumbHierarchy hierarchy = BreadcrumbsSettings.getBreadcrumbsHierarchy();
                List<Breadcrumb> crumbs = getBreadcrumbs();

                List<DisplayedBreadcrumb> collapsed = collapser.collapse(crumbs);
                List<DisplayedBreadcrumb> missing = hierarchy.restoreMissingHierarchy(crumbs);
                if (missing.isEmpty()) {
                    return collapsed;
                }

                List<DisplayedBreadcrumb> result = new ArrayList<DisplayedBreadcrumb>(missing.size() + collapsed.size());
                result.addAll(missing);
                result.addAll(collapsed);
                return result;
            }
        }) {
            
			private static final long serialVersionUID = 1L;

			@Override
            protected void populateItem(ListItem<DisplayedBreadcrumb> item) {
                BreadcrumbsPanel.this.populateBreadcrumb(item);
            }
        });
    }

    // PRIVATE API
    List<Breadcrumb> getBreadcrumbs() {
        return BreadcrumbTrailHistory.getTrail(Breadcrumb.constructIdFrom(getPage()));
    }

    protected void populateBreadcrumb(ListItem<DisplayedBreadcrumb> item) {
        DisplayedBreadcrumb crumb = item.getModelObject();
        Component title = new Label("title", crumb.title())
            .setEscapeModelStrings(crumb.shouldEscapeTitle());

        final Fragment crumbContainer;
        if (item.getParent() == breadcrumbs) {
            if (shouldBeDisabled(item)) {
                crumbContainer = getCrumbSingleInactive("crumb");
                
            } else {
                crumbContainer = getCrumbSingle("crumb");
            }
        } else {
            crumbContainer = getCrumbSingleCollapsed("crumb");
        }

        if (item.getModelObject().isCollapsed()) {
            WebMarkupContainer collapsed = getCrumbCollapsed("crumb");
            IModel<List<DisplayedBreadcrumb>> collapsedCrumbs = PropertyModel.of(item.getModel(), "collapsedCrumbs");
            collapsed.add(new ListView<DisplayedBreadcrumb>("crumbs", collapsedCrumbs) {
                @Override
                protected void populateItem(ListItem<DisplayedBreadcrumb> item1) {
                    BreadcrumbsPanel.this.populateBreadcrumb(item1);
                }
            });
            collapsed.add(crumbContainer.add(new WebMarkupContainer("link").add(title)));

            item.add(collapsed);
        } else {
            AbstractLink link = customize(createBreadcrumbLink("link", item));
            if (shouldBeDisabled(item)) {
            	item.add(new AttributeAppender("class", "active"));
                markAsDisabled(link);
            }
            crumbContainer.add(link.add(title));

            item.add(crumbContainer);
        }
    }
    
    protected Fragment getCrumbSingleInactive(String id){
    	return new Fragment(id, "singleInactive", this);
    }
    
    protected Fragment getCrumbSingle(String id){
    	return new Fragment(id, "single", this);
    }
    
    protected Fragment getCrumbSingleCollapsed(String id){
    	return new Fragment(id, "singleCollapsed", this);
    }
    
    protected Fragment getCrumbCollapsed(String id){
    	return new Fragment(id, "collapsed", this);
    }

    protected AbstractLink customize(AbstractLink link) {
        return link.setBeforeDisabledLink("").setAfterDisabledLink("");
    }

    protected void markAsDisabled(WebMarkupContainer link) {
        link.setEnabled(false);
        link.add(new AttributeAppender("class", " inactive"));
    }

    protected boolean shouldBeDisabled(ListItem<DisplayedBreadcrumb> item) {
        ListView<?> parent = item.findParent(ListView.class);
        if (parent == breadcrumbs) {
            return parent.getViewSize() - 1 == item.getIndex();
        }
        return false;
    }

    protected static final AbstractLink createBreadcrumbLink(String linkId, ListItem<DisplayedBreadcrumb> item) {
        return new BreadcrumbLink(linkId, new PropertyModel<IBreadcrumbTargetProvider>(item.getModel(), "targetProvider"));
    }

}
