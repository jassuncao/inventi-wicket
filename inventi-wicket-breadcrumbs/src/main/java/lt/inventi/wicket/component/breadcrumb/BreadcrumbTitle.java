package lt.inventi.wicket.component.breadcrumb;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public final class BreadcrumbTitle implements Serializable {
    
	private static final long serialVersionUID = -8141689855714187894L;
	private final IModel<String> title;
    private final boolean shouldEscapeTitle;

    public BreadcrumbTitle(IModel<String> title) {
        this(title, true);
    }

    public BreadcrumbTitle(IModel<String> title, boolean shouldEscapeTitle) {
        this.title = title;
        this.shouldEscapeTitle = shouldEscapeTitle;
    }

    public IModel<String> getTitle() {
        return title;
    }

    public boolean shouldEscapeTitle() {
        return shouldEscapeTitle;
    }

    @Override
    public String toString() {
        return (shouldEscapeTitle ? "Escaped" : "Unescaped") + " title<" + title + ">";
    }
}
