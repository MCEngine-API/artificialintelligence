package io.github.mcengine.api.artificialintelligence.function.calling;

import java.util.List;

/**
 * Represents a rule for matching player input to a static chatbot response.
 * Each rule contains a list of possible match strings and a single response.
 */
public class FunctionRule {

    /**
     * A list of input phrases or patterns that should trigger this rule.
     */
    private List<String> match;

    /**
     * The chatbot's response when this rule is triggered.
     */
    private String response;

    /**
     * Constructs a FunctionRule with the specified matching inputs and response text.
     *
     * @param match    A list of strings that represent valid triggers for this rule.
     * @param response The response string returned when this rule matches.
     */
    public FunctionRule(List<String> match, String response) {
        this.match = match;
        this.response = response;
    }

    /**
     * Gets the list of input phrases or patterns that trigger this rule.
     *
     * @return the list of match strings
     */
    public List<String> getMatch() {
        return match;
    }

    /**
     * Gets the chatbot's response for this rule.
     *
     * @return the response string
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the list of input phrases or patterns that trigger this rule.
     *
     * @param match the new list of match strings
     */
    public void setMatch(List<String> match) {
        this.match = match;
    }

    /**
     * Sets the chatbot's response for this rule.
     *
     * @param response the new response string
     */
    public void setResponse(String response) {
        this.response = response;
    }
}
