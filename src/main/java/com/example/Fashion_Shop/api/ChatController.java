package com.example.Fashion_Shop.api;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}")
public class ChatController {
    // Đối tượng để tương tác với LLM(OpenAi)
    private final ChatClient chatClient;

    // Đối tượng sử dụng để truy vấn thông tin
    private final QdrantVectorStore qdrantVectorStore;
    String context = "";

    public ChatController(ChatClient chatClient, QdrantVectorStore draQdrantVectorStore) {
        this.chatClient = chatClient;
        this.qdrantVectorStore = draQdrantVectorStore;
    }

    @GetMapping("/chat-ai")
    public ResponseEntity<Map<String, String>> doChat(@RequestParam String message) {
        String context = ""; // Reset context mỗi khi có request mới
        try {
            // B1. Truy vấn từ cơ sở kiến thức với từ khóa là câu hỏi của người dùng
            List<Document> contexts = qdrantVectorStore.similaritySearch(
                    SearchRequest.query(message).withTopK(10)
            );

            // B2. Tạo nội dung context từ các thông tin trả về
            for (Document document : contexts) {
                context += document.getContent() + " ";
            }

            // B3. Gọi API LLM để nhận câu trả lời
            String finalContext = context;
            String answer = chatClient.prompt()
                    .system(sp -> sp.param("context", finalContext)) // Cung cấp nội dung context
                    .user(message)
                    .call()
                    .content();

            // Trả về kết quả dạng JSON
            Map<String, String> response = new HashMap<>();
            response.put("content", answer);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Bắt và xử lý lỗi nếu xảy ra
            e.printStackTrace();

            // Trả về thông báo lỗi
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
