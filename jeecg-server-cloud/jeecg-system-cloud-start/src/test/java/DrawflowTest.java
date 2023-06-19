import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.drawflow.Drawflow;
import org.jeecg.modules.polymerize.drawflow.model.DrawflowNode;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/6/6 13:58
 */
@Slf4j
public class DrawflowTest {

    public static void main(String[] args) {
        try {
            Drawflow d = new Drawflow("{\"drawflow\":{\"Home\":{\"data\":{\"11\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[]}},\"pos_y\":9,\"pos_x\":893,\"data\":{},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"10\",\"input\":\"output_1\"}]}},\"name\":\"ListRuleNode\",\"html\":\"ListRuleNode\",\"id\":11,\"class\":\"ListRuleNode\"},\"1\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"2\"}]}},\"pos_y\":256,\"pos_x\":190,\"data\":{},\"inputs\":{},\"name\":\"StartNode\",\"html\":\"StartNode\",\"id\":1,\"class\":\"StartNode\"},\"12\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[]}},\"pos_y\":673,\"pos_x\":875,\"data\":{},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"9\",\"input\":\"output_1\"}]}},\"name\":\"ListRuleNode\",\"html\":\"ListRuleNode\",\"id\":12,\"class\":\"ListRuleNode\"},\"2\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"3\"},{\"output\":\"input_1\",\"node\":\"4\"},{\"output\":\"input_1\",\"node\":\"5\"}]}},\"pos_y\":294,\"pos_x\":586,\"data\":{\"startUrls\":\"起始URL集合\",\"nextMatch\":\"下一页按钮匹配\",\"customConfig\":\"自定义配置\",\"articleTitleMatch\":\"稿件标题匹配\",\"articleDateMatch\":\"稿件日期匹配\",\"preMatch\":\"上一页按钮匹配\",\"articleUrlMatch\":\"稿件url匹配\",\"pageMatch\":\"通用分页匹配\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"1\",\"input\":\"output_1\"}]}},\"name\":\"ListRuleNode\",\"html\":\"ListRuleNode\",\"id\":2,\"class\":\"ListRuleNode\"},\"3\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"8\"}]}},\"pos_y\":143,\"pos_x\":1096,\"data\":{\"referenceMatch\":\"稿件出处匹配\",\"commentMatch\":\"稿件评论量匹配\",\"contentMatch\":\"稿件正文匹配\",\"keywordsMatch\":\"稿件关键词匹配\",\"sourceMatch\":\"稿件来源匹配\",\"visitMatch\":\"稿件访问量匹配\",\"authorMatch\":\"稿件作者匹配\",\"collectMatch\":\"稿件收藏量匹配\",\"titleMatch\":\"稿件标题匹配\",\"urlMatch\":\"稿件url匹配\",\"subtitleMatch\":\"稿件副标题匹配\",\"dateMatch\":\"稿件日期匹配\",\"customConfig\":\"自定义配置\",\"descriptionMatch\":\"稿件描述匹配\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"2\",\"input\":\"output_1\"}]}},\"name\":\"ArticleRuleNode\",\"html\":\"ArticleRuleNode\",\"id\":3,\"class\":\"ArticleRuleNode\"},\"4\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"8\"}]}},\"pos_y\":546,\"pos_x\":1108,\"data\":{\"referenceMatch\":\"333\",\"commentMatch\":\"333\",\"contentMatch\":\"333\",\"keywordsMatch\":\"333\",\"sourceMatch\":\"333\",\"visitMatch\":\"333\",\"authorMatch\":\"333\",\"collectMatch\":\"333\",\"titleMatch\":\"333\",\"urlMatch\":\"333\",\"subtitleMatch\":\"333\",\"dateMatch\":\"333\",\"customConfig\":\"333\",\"descriptionMatch\":\"333\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"2\",\"input\":\"output_1\"}]}},\"name\":\"ArticleRuleNode\",\"html\":\"ArticleRuleNode\",\"id\":4,\"class\":\"ArticleRuleNode\"},\"5\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"8\"}]}},\"pos_y\":350,\"pos_x\":1105,\"data\":{\"referenceMatch\":\"2222222\",\"commentMatch\":\"2222222\",\"contentMatch\":\"2222222\",\"keywordsMatch\":\"2222222\",\"sourceMatch\":\"2222222\",\"visitMatch\":\"2222222\",\"authorMatch\":\"2222222\",\"collectMatch\":\"2222222\",\"titleMatch\":\"2222222\",\"urlMatch\":\"2222222\",\"subtitleMatch\":\"2222222\",\"dateMatch\":\"2222222\",\"customConfig\":\"2222222\",\"descriptionMatch\":\"2222222\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"2\",\"input\":\"output_1\"}]}},\"name\":\"ArticleRuleNode\",\"html\":\"ArticleRuleNode\",\"id\":5,\"class\":\"ArticleRuleNode\"},\"8\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[]}},\"pos_y\":359,\"pos_x\":1510,\"data\":{\"startUrls\":\"起始URL集合222\",\"nextMatch\":\"下一页按钮匹配222\",\"customConfig\":\"自定义配置222\",\"articleTitleMatch\":\"稿件标题匹配222\",\"articleDateMatch\":\"稿件日期匹配222\",\"preMatch\":\"上一页按钮匹配222\",\"articleUrlMatch\":\"下一页按钮匹配222\",\"pageMatch\":\"通用分页匹配222\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"5\",\"input\":\"output_1\"},{\"node\":\"3\",\"input\":\"output_1\"},{\"node\":\"4\",\"input\":\"output_1\"}]}},\"name\":\"ListRuleNode\",\"html\":\"ListRuleNode\",\"id\":8,\"class\":\"ListRuleNode\"},\"9\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"12\"}]}},\"pos_y\":693,\"pos_x\":612,\"data\":{},\"inputs\":{},\"name\":\"StartNode\",\"html\":\"StartNode\",\"id\":9,\"class\":\"StartNode\"},\"10\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"11\"}]}},\"pos_y\":63,\"pos_x\":593,\"data\":{},\"inputs\":{},\"name\":\"StartNode\",\"html\":\"StartNode\",\"id\":10,\"class\":\"StartNode\"}}}}}");
            // d.test();
            log.info("哈哈哈哈哈");
            int i = 0;
            while (d.hasNext()) {
                if (i == 1)  {
                    break;
                }
                DrawflowNode n = d.next();
                log.info(n.toString());
                i++;
            }
            while (d.hasNext()) {
                DrawflowNode n = d.next();
                log.info(n.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

}
