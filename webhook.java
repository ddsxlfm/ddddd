@Slf4j
@RestController
public class WebhookController {

    private static String WECHAT_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=931a3d79-ac5b-4919-a3d9-912629adac7f;"

    private static String GITHUB_API = "https://api.github.com/users/";

    /**
     * @param webhook webhook
     * @author 程序员内点事
     * @Description: github 回调
     * @date 2021/05/19
     */
    @PostMapping("/webhook")
    public String webhookGithub(@RequestBody GithubWebhookPullVo webhook) {

        log.info("webhook 入参接收 weChatWebhook {}", JSON.toJSONString(webhook));
        // 仓库名
        String name = webhook.getRepository().getName();
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = simpleFormatter.format(new Date());
        String content = null;
        if (webhook.getCommits().size() > 0) {
            GithubWebhookPullVo.CommitsDTO commitsDTO = webhook.getCommits().get(0);

            content = "[" + commitsDTO.getCommitter().getName() + "]" +
                    "于：" + now + "，" +
                    "向作者：[" + commitsDTO.getAuthor().getName() + "]的，远程仓库" + name + "推送代码" +
                    "详情：";

            List<String> addeds = commitsDTO.getAdded();
            if (addeds.size() > 0) {
                content += "添加文件:";
                for (int i = 0; i < addeds.size(); i++) {
                    content = (i + 1) + content + addeds.get(i);
                }
            }
            List<String> modifieds = commitsDTO.getModified();
            if (modifieds.size() > 0) {
                content += "修改文件:";
                for (int i = 0; i < modifieds.size(); i++) {
                    content = (i + 1) + content + modifieds.get(i);
                }
            }
            List<String> removeds = commitsDTO.getRemoved();
            if (removeds.size() > 0) {
                content += "删除文件:";
                for (int i = 0; i < removeds.size(); i++) {
                    content = (i + 1) + content + removeds.get(i);
                }
            }
        }
        log.info(content);

        WeChatWebhook weChatWebhook = new WeChatWebhook();
        weChatWebhook.setMsgtype("text");
        WeChatWebhook.TextDTO textDTO = new WeChatWebhook.TextDTO();
        textDTO.setContent(content);
        textDTO.setMentionedList(Arrays.asList("@all"));
        textDTO.setMentionedMobileList(Arrays.asList("@all"));
        weChatWebhook.setText(textDTO);

        /**
         * 组装参数后向企业微信发送webhook请求
         */
        log.info("企业微信发送参数 {}", JSON.toJSONString(weChatWebhook));
        String post = HttpUtil.sendPostJsonBody(WECHAT_URL, JSON.toJSONString(weChatWebhook));
        log.info("企业微信发送结果 post {}", post);
        return JSON.toJSONString(post);
    }
}
