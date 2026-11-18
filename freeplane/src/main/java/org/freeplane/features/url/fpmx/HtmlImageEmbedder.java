package org.freeplane.features.url.fpmx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlImageEmbedder {
	public interface SourceResolver {
		String embeddedSourceFor(String source);
	}

	public static class Result {
		public final String html;
		public final int embeddedCount;

		Result(String html, int embeddedCount) {
			this.html = html;
			this.embeddedCount = embeddedCount;
		}
	}

	private static final Pattern IMAGE_SOURCE = Pattern.compile("(<img\\b[^>]*?\\bsrc\\s*=\\s*)([\"'])(.*?)\\2",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	public static Result embed(String html, SourceResolver resolver) {
		if (html == null)
			return new Result(null, 0);
		final Matcher matcher = IMAGE_SOURCE.matcher(html);
		final StringBuffer result = new StringBuffer();
		int count = 0;
		while (matcher.find()) {
			final String source = matcher.group(3);
			final String embedded = source.regionMatches(true, 0, "data:", 0, 5) ? null
			        : resolver.embeddedSourceFor(source);
			if (embedded == null)
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
			else {
				count++;
				matcher.appendReplacement(result,
				    Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + embedded + matcher.group(2)));
			}
		}
		matcher.appendTail(result);
		return new Result(count == 0 ? html : result.toString(), count);
	}
}
