package youyihj.probezs.socket.rpc;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author youyihj
 */
public class RpcCodec extends MessageToMessageCodec<String, RpcResponse> {
    private final Gson gson;
    private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("Content-Length: ([0-9]*)");

    public RpcCodec(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse msg, List<Object> out) throws Exception {
        String resultString = gson.toJson(msg);
        int contentLength = resultString.getBytes(StandardCharsets.UTF_8).length;
        out.add("Content-Length: " + contentLength + "\r\n\r\n" + resultString);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        while (!msg.isEmpty()) {
            String[] split = msg.split("\r\n\r\n", 2);
            String header = split[0];
            Matcher matcher = CONTENT_LENGTH_PATTERN.matcher(header);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not find Content-Length header in " + msg);
            }
            String content = new String(split[1].getBytes(StandardCharsets.UTF_8), 0, Integer.parseInt(matcher.group(1)));
            out.add(gson.fromJson(content, RpcRequest.class));
            if (split[1].length() > content.length()) {
                msg = split[1].substring(content.length());
            } else {
                msg = "";
            }
        }
    }
}
