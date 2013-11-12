package lt.inventi.wicket.component.breadcrumb;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;


public class NextBookmarkablePageLink<T> extends BookmarkablePageLink<T> {
   
	private static final long serialVersionUID = -8654814624201915523L;

	public <C extends Page> NextBookmarkablePageLink(String id, Class<C> pageClass, PageParameters parameters) {
        super(id, pageClass, parameters);
    }

    public <C extends Page> NextBookmarkablePageLink(String id, Class<C> pageClass) {
        super(id, pageClass);
    }

    @Override
    protected void onInitialize() {
        BreadcrumbPageParameters.setTrailTo(getPageParameters(), getPage());
        super.onInitialize();
    }
}
