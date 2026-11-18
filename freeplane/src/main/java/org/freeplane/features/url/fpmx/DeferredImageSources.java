package org.freeplane.features.url.fpmx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeferredImageSources {
	public static final String PREFIX = "fpmxpending:";
	static final int MINIMUM_LENGTH = 20000;
	private static final Pattern LARGE_DATA_URI = Pattern
	    .compile("data:image/[a-z0-9.+-]+;base64,[A-Za-z0-9+/=]{" + MINIMUM_LENGTH + ",}");

	private static final Pattern PLACEHOLDER = Pattern.compile(Pattern.quote(PREFIX) + "[0-9]+");

	public final String html;
	public final Map<String, String> sources;

	private DeferredImageSources(String html, Map<String, String> sources) {
		this.html = html;
		this.sources = sources;
	}

	public static DeferredImageSources defer(String html) {
		final Map<String, String> sources = new LinkedHashMap<>();
		final Matcher matcher = LARGE_DATA_URI.matcher(html);
		final StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			final String placeholder = PREFIX + sources.size();
			sources.put(placeholder, matcher.group());
			matcher.appendReplacement(result, Matcher.quoteReplacement(placeholder));
		}
		matcher.appendTail(result);
		return new DeferredImageSources(sources.isEmpty() ? html : result.toString(), sources);
	}

	public String restoreIn(String text) {
		if (isEmpty())
			return text;
		final Matcher matcher = PLACEHOLDER.matcher(text);
		final StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			final String source = sources.get(matcher.group());
			matcher.appendReplacement(result, Matcher.quoteReplacement(source != null ? source : matcher.group()));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	public boolean isEmpty() {
		return sources.isEmpty();
	}
}
