package bilal.com.createdynamicwidgets;

/**
 * Created by BILAL on 11/28/2017.
 */

public class ServeyModel {

    String ServeyTitle;

    String QuestionTitle;

    String servey_id;

    String question_id;

    String question_type;

    String answer_type;

    String created_at;

    String image;

    String options;

    String type;

    String take_image = "";

    public ServeyModel(String serveyTitle, String questionTitle, String servey_id, String question_id, String question_type, String answer_type, String created_at, String options,String image,String type) {
        ServeyTitle = serveyTitle;
        QuestionTitle = questionTitle;
        this.servey_id = servey_id;
        this.question_id = question_id;
        this.question_type = question_type;
        this.answer_type = answer_type;
        this.created_at = created_at;
        this.options = options;
        this.image = image;
        this.type = type;
    }


    public ServeyModel(String serveyTitle, String type) {
        ServeyTitle = serveyTitle;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setServeyTitle(String serveyTitle) {
        ServeyTitle = serveyTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        QuestionTitle = questionTitle;
    }

    public void setServey_id(String servey_id) {
        this.servey_id = servey_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public void setQuestion_type(String question_type) {
        this.question_type = question_type;
    }

    public void setAnswer_type(String answer_type) {
        this.answer_type = answer_type;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getServeyTitle() {
        return ServeyTitle;
    }

    public String getQuestionTitle() {
        return QuestionTitle;
    }

    public String getServey_id() {
        return servey_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public String getQuestion_type() {
        return question_type;
    }

    public String getAnswer_type() {
        return answer_type;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getOptions() {
        return options;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setTake_image(String take_image) {
        this.take_image = take_image;
    }

    public String getTake_image() {
        return take_image;
    }
}
