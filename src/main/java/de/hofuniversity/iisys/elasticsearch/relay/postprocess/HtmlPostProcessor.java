package de.hofuniversity.iisys.elasticsearch.relay.postprocess;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import de.hofuniversity.iisys.elasticsearch.relay.util.ESConstants;

public class HtmlPostProcessor implements IPostProcessor
{
    private static final String NOTE_FIELD = "note:note";

    @Override
    public JSONObject process(JSONObject result) throws Exception
    {
        JSONObject source = result.getJSONObject(ESConstants.R_HIT_SOURCE);
        
        if(source.has(NOTE_FIELD))
        {
            String content = getProcessed(source.getString(NOTE_FIELD));
            source.remove(NOTE_FIELD);
            source.put(NOTE_FIELD, content);
        }
        
        return result;
    }

    private String getProcessed(String body)
    {
        String output = "";
        
        Document doc = Jsoup.parse(body);

        // stateful - do not reuse without thread safety
        FormattingVisitor fFormatter = new FormattingVisitor();
        NodeTraversor fTraversor = new NodeTraversor(fFormatter);
        
        // walk the DOM, and call .head() and .tail() for each node
        fTraversor.traverse(doc.body());

        output = fFormatter.toString();
        
        return output;
    }
    
    
    
    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append("  ");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n");
           else if (name.equals("a"))
                append(" ");
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
}
