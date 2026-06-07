package compiler.ai;

public class AIErrorAssistant {

    private final String apiKey;
    private final boolean realMode;

    public AIErrorAssistant() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
        this.realMode = (apiKey != null && !apiKey.isEmpty());
    }

    public String explainError(String errorMessage, String sourceContext, int line) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "";
        }
        if (realMode) {
            return callLLM(errorMessage, sourceContext, line);
        }
        return mockExplain(errorMessage, line);
    }

    private String mockExplain(String error, int line) {
        StringBuilder sb = new StringBuilder();
        sb.append("[AI Assistant] Error analysis at line ").append(line).append(":\n");

        if (error.contains("undefined variable") || error.contains("Undefined variable")) {
            String varName = extractVarName(error);
            sb.append("- Variable '").append(varName).append("' is not defined in the current scope.\n");
            sb.append("- Check: 1) Did you declare it with 'let'? 2) Is it in the current block scope?\n");
            sb.append("- Fix: Add 'let ").append(varName).append(" = <value>;' before usage.\n");
        } else if (error.contains("unexpected character")) {
            sb.append("- The lexer encountered an unrecognized character.\n");
            sb.append("- Supported: + - * / % ( ) { } = ; , ! < > & |\n");
        } else if (error.contains("Expect")) {
            sb.append("- Parse error: missing expected token (e.g., ';', ')', '}').\n");
            sb.append("- Check for missing punctuation or braces near this line.\n");
        } else if (error.contains("Runtime error")) {
            sb.append("- Runtime execution error.\n");
            sb.append("- Possible: type mismatch, undefined variable, or bad operation.\n");
        } else {
            sb.append("- ").append(error).append("\n");
        }

        return sb.toString();
    }

    private String extractVarName(String error) {
        int start = error.indexOf("'") + 1;
        int end = error.indexOf("'", start);
        if (start > 0 && end > start) {
            return error.substring(start, end);
        }
        return "unknown";
    }

    private String callLLM(String error, String sourceContext, int line) {
        try {
            String prompt = "You are a compiler error assistant. A MiniLang program produced an error:\n\n"
                    + "Error: " + error + "\n"
                    + "At line: " + line + "\n"
                    + "Source:\n" + sourceContext + "\n\n"
                    + "Explain the error and suggest a fix.";

            java.net.URL url = new java.net.URL("https://api.openai.com/v1/chat/completions");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String escaped = prompt.replace("\\", "\\\\")
                                   .replace("\"", "\\\"")
                                   .replace("\n", "\\n")
                                   .replace("\t", "\\t");
            String body = "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\""
                        + escaped + "\"}],\"temperature\":0.3}";

            conn.getOutputStream().write(body.getBytes("utf-8"));

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String lineStr;
            while ((lineStr = reader.readLine()) != null) {
                response.append(lineStr);
            }
            reader.close();

            String resp = response.toString();
            int start = resp.indexOf("\"content\":\"");
            if (start > 0) {
                start += 11;
                int end = resp.indexOf("\"", start);
                return resp.substring(start, end).replace("\\n", "\n");
            }
            return resp;
        } catch (Exception e) {
            return "[AI Error] " + e.getMessage();
        }
    }
}
