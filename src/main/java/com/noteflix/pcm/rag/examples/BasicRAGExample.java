package com.noteflix.pcm.rag.examples;

import com.noteflix.pcm.rag.api.RAGService;
import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.api.VectorStoreConfig;
import com.noteflix.pcm.rag.api.VectorStoreFactory;
import com.noteflix.pcm.rag.core.DefaultRAGService;
import com.noteflix.pcm.rag.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Basic RAG example - 100% offline.
 * 
 * Shows how to:
 * 1. Create offline vector store (Lucene)
 * 2. Index documents
 * 3. Query and get results
 * 
 * Run this to test the RAG system!
 * 
 * @author PCM Team
 * @version 1.0.0
 */
public class BasicRAGExample {
    
    public static void main(String[] args) {
        System.out.println("=== Basic RAG Example (Offline) ===\n");
        
        try {
            // 1. Create offline vector store (Lucene)
            System.out.println("📦 Creating Lucene vector store (offline)...");
            VectorStore vectorStore = VectorStoreFactory.create(
                VectorStoreConfig.lucene("data/rag/example-index")
            );
            
            // 2. Create RAG service
            System.out.println("🤖 Creating RAG service...");
            RAGService ragService = new DefaultRAGService(vectorStore);
            
            // 3. Index some sample documents
            System.out.println("📝 Indexing sample documents...\n");
            indexSampleDocuments(ragService);
            
            System.out.println("✅ Indexed " + ragService.getDocumentCount() + " documents\n");
            
            // 4. Query examples
            System.out.println("=== Query Examples ===\n");
            
            // Query 1: Search for customer service
            query(ragService, "customer service validation");
            
            // Query 2: Search for batch jobs
            query(ragService, "batch job schedule");
            
            // Query 3: Search for database
            query(ragService, "database procedure");
            
            // 5. Cleanup
            System.out.println("\n🧹 Cleaning up...");
            ragService.close();
            
            System.out.println("\n✅ Example completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void indexSampleDocuments(RAGService ragService) {
        // Sample 1: Java code
        RAGDocument doc1 = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.JAVA_CLASS)
            .title("CustomerService.java")
            .sourcePath("src/main/java/com/example/CustomerService.java")
            .content(
                "public class CustomerService {\n" +
                "    public void validateCustomer(Customer customer) {\n" +
                "        if (customer.getName() == null) {\n" +
                "            throw new ValidationException(\"Name is required\");\n" +
                "        }\n" +
                "        if (customer.getEmail() == null) {\n" +
                "            throw new ValidationException(\"Email is required\");\n" +
                "        }\n" +
                "    }\n" +
                "}"
            )
            .indexedAt(LocalDateTime.now())
            .build();
        
        doc1.addMetadata("package", "com.example");
        doc1.addMetadata("class", "CustomerService");
        
        // Sample 2: Database procedure
        RAGDocument doc2 = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.PROCEDURE)
            .title("P_INSERT_CUSTOMER")
            .sourcePath("database/procedures/customer_pkg.sql")
            .content(
                "CREATE OR REPLACE PROCEDURE P_INSERT_CUSTOMER(\n" +
                "    p_name IN VARCHAR2,\n" +
                "    p_email IN VARCHAR2,\n" +
                "    p_phone IN VARCHAR2\n" +
                ") IS\n" +
                "BEGIN\n" +
                "    INSERT INTO CUSTOMERS (name, email, phone, created_at)\n" +
                "    VALUES (p_name, p_email, p_phone, SYSDATE);\n" +
                "    COMMIT;\n" +
                "END;"
            )
            .indexedAt(LocalDateTime.now())
            .build();
        
        doc2.addMetadata("schema", "PROD");
        doc2.addMetadata("package", "CUSTOMER_PKG");
        
        // Sample 3: Batch job
        RAGDocument doc3 = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.BATCH_JOB)
            .title("Daily Customer Import")
            .sourcePath("config/batch-jobs/customer-import.xml")
            .content(
                "Batch Job: Daily Customer Import\n" +
                "Schedule: 0 2 * * * (Every day at 2:00 AM)\n" +
                "Description: Import customer data from external file\n" +
                "Steps:\n" +
                "1. Read CSV file from /data/import/customers.csv\n" +
                "2. Validate customer data\n" +
                "3. Insert into CUSTOMERS table\n" +
                "4. Send notification email\n" +
                "Dependencies: File Arrival Check Job"
            )
            .indexedAt(LocalDateTime.now())
            .build();
        
        doc3.addMetadata("schedule", "0 2 * * *");
        doc3.addMetadata("subsystem", "CUSTOMER");
        
        // Sample 4: Knowledge base
        RAGDocument doc4 = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.KNOWLEDGE_BASE)
            .title("Customer Registration Troubleshooting")
            .sourcePath("docs/kb/customer-registration.md")
            .content(
                "# Customer Registration Troubleshooting\n\n" +
                "## Common Issues\n\n" +
                "### Email validation fails\n" +
                "- Check email format\n" +
                "- Verify domain exists\n" +
                "- Check for duplicate emails in database\n\n" +
                "### Database procedure fails\n" +
                "- Check P_INSERT_CUSTOMER procedure\n" +
                "- Verify all required fields are provided\n" +
                "- Check database connection\n\n" +
                "### Screen freezes\n" +
                "- Check CustomerService.validateCustomer()\n" +
                "- Review application logs\n" +
                "- Contact support team"
            )
            .indexedAt(LocalDateTime.now())
            .build();
        
        doc4.addMetadata("category", "troubleshooting");
        doc4.addMetadata("subsystem", "CUSTOMER");
        
        // Index all documents
        ragService.indexDocument(doc1);
        ragService.indexDocument(doc2);
        ragService.indexDocument(doc3);
        ragService.indexDocument(doc4);
        
        System.out.println("  ✓ Indexed: " + doc1.getTitle());
        System.out.println("  ✓ Indexed: " + doc2.getTitle());
        System.out.println("  ✓ Indexed: " + doc3.getTitle());
        System.out.println("  ✓ Indexed: " + doc4.getTitle());
    }
    
    private static void query(RAGService ragService, String query) {
        System.out.println("🔍 Query: \"" + query + "\"");
        
        RAGResponse response = ragService.query(query);
        
        System.out.println("📊 Results:");
        System.out.println("  - Documents found: " + response.getDocumentsRetrieved());
        System.out.println("  - Processing time: " + response.getProcessingTimeMs() + "ms");
        System.out.println("  - Confidence: " + String.format("%.1f%%", response.getConfidence() * 100));
        System.out.println("\n📝 Answer:\n" + response.getAnswer());
        System.out.println("─".repeat(80) + "\n");
    }
}

