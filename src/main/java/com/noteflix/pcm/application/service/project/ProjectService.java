package com.noteflix.pcm.application.service.project;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Project Service - Handles project data operations
 * 
 * Provides asynchronous operations for loading project information,
 * simulating real-world API calls and database operations.
 */
@Slf4j
public class ProjectService {

  /**
   * Loads project information asynchronously
   * @param projectCode The project code (e.g., "CS", "OM", "PG")
   * @return CompletableFuture containing project data
   */
  public CompletableFuture<ProjectData> loadProjectAsync(String projectCode) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        log.info("Loading project data for: {}", projectCode);
        
        // Simulate network/database delay
        Thread.sleep(2000);
        
        // Simulate potential loading failure (5% chance)
        if (Math.random() < 0.05) {
          throw new RuntimeException("Failed to load project data from server");
        }
        
        ProjectData projectData = createMockProjectData(projectCode);
        log.info("Successfully loaded project data for: {}", projectCode);
        
        return projectData;
        
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Project loading was interrupted", e);
      }
    });
  }

  /**
   * Loads all projects asynchronously
   * @return CompletableFuture containing list of all projects
   */
  public CompletableFuture<List<ProjectData>> loadAllProjectsAsync() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        log.info("Loading all projects data");
        
        // Simulate network delay
        Thread.sleep(1500);
        
        List<ProjectData> projects = List.of(
            createMockProjectData("CS"),
            createMockProjectData("OM"),
            createMockProjectData("PG"),
            createMockProjectData("IA"),
            createMockProjectData("RP")
        );
        
        log.info("Successfully loaded {} projects", projects.size());
        return projects;
        
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Projects loading was interrupted", e);
      }
    });
  }

  /**
   * Creates mock project data based on project code
   */
  private ProjectData createMockProjectData(String projectCode) {
    return switch (projectCode) {
      case "CS" -> ProjectData.builder()
          .code("CS")
          .name("Customer Service")
          .description("Complete customer support and service management system")
          .screenCount(24)
          .status("Active")
          .teamSize(8)
          .lastUpdated(java.time.LocalDateTime.now().minusDays(1))
          .features(List.of(
              "Customer ticket management and tracking",
              "Live chat and messaging system",
              "Knowledge base and FAQ management",
              "Customer profile and history tracking",
              "Service level agreement (SLA) monitoring",
              "Agent dashboard and workload distribution"
          ))
          .build();
          
      case "OM" -> ProjectData.builder()
          .code("OM")
          .name("Order Management")
          .description("Comprehensive e-commerce order processing system")
          .screenCount(18)
          .status("Active")
          .teamSize(6)
          .lastUpdated(java.time.LocalDateTime.now().minusHours(3))
          .features(List.of(
              "Order creation and processing workflow",
              "Inventory management and stock tracking",
              "Payment gateway integration",
              "Shipping and fulfillment management",
              "Customer order history and tracking",
              "Return and refund processing"
          ))
          .build();
          
      case "PG" -> ProjectData.builder()
          .code("PG")
          .name("Payment Gateway")
          .description("Secure payment processing system")
          .screenCount(12)
          .status("Development")
          .teamSize(4)
          .lastUpdated(java.time.LocalDateTime.now().minusHours(6))
          .features(List.of(
              "Multi-payment method support",
              "PCI DSS compliance and security",
              "Real-time fraud detection",
              "Payment reconciliation and settlement",
              "Chargeback and dispute management",
              "Transaction monitoring and analytics"
          ))
          .build();
          
      case "IA" -> ProjectData.builder()
          .code("IA")
          .name("Inventory Admin")
          .description("Advanced inventory management system")
          .screenCount(15)
          .status("Active")
          .teamSize(5)
          .lastUpdated(java.time.LocalDateTime.now().minusDays(2))
          .features(List.of(
              "Real-time inventory tracking",
              "Warehouse location and bin management",
              "Automated reorder point notifications",
              "Supplier and vendor management",
              "Stock movement and audit trails",
              "Barcode and QR code scanning"
          ))
          .build();
          
      case "RP" -> ProjectData.builder()
          .code("RP")
          .name("Reports Portal")
          .description("Business intelligence and analytics platform")
          .screenCount(8)
          .status("Planning")
          .teamSize(3)
          .lastUpdated(java.time.LocalDateTime.now().minusDays(3))
          .features(List.of(
              "Interactive dashboard and visualization",
              "Custom report builder and scheduling",
              "Real-time KPI monitoring and alerts",
              "Cross-system data integration",
              "Export capabilities (PDF, Excel, CSV)",
              "Role-based access and permissions"
          ))
          .build();
          
      default -> throw new IllegalArgumentException("Unknown project code: " + projectCode);
    };
  }
}