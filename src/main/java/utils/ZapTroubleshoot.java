package utils;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

public class ZapTroubleshoot {
    
    public static void main(String[] args) {
        String zapAddress = "127.0.0.1";
        int zapPort = 8080;
        String apiKey = "5vl21k70dtpa3nuglm41netd6f";
        
        System.out.println("=== ZAP Connection Troubleshoot ===");
        System.out.println("ZAP Address: " + zapAddress);
        System.out.println("ZAP Port: " + zapPort);
        System.out.println("API Key: " + apiKey);
        System.out.println();
        
        // Test basic connection
        testConnection(zapAddress, zapPort, apiKey);
        
        // Test without API key
        testConnection(zapAddress, zapPort, null);
        
        // Test different ports
        int[] commonPorts = {8080, 8090, 8082};
        for (int port : commonPorts) {
            System.out.println("Trying port: " + port);
            testConnection(zapAddress, port, apiKey);
        }
        
        System.out.println("\n=== Troubleshooting Tips ===");
        System.out.println("1. Make sure OWASP ZAP is running");
        System.out.println("2. Check if ZAP is running on the correct port (default: 8080)");
        System.out.println("3. Verify API key in ZAP: Tools -> Options -> API");
        System.out.println("4. Enable API in ZAP: Tools -> Options -> API -> Enable API");
        System.out.println("5. Check firewall settings");
        System.out.println("6. Try starting ZAP with: java -jar zap.jar -daemon -port 8081 -config api.key=" + apiKey);
    }
    
    private static void testConnection(String address, int port, String apiKey) {
        try {
            System.out.println("Testing connection to " + address + ":" + port + " with API key: " + 
                             (apiKey != null ? "***" : "none"));
            
            ClientApi clientApi = new ClientApi(address, port, apiKey);
            ApiResponse response = clientApi.core.version();
            String version = ((ApiResponseElement) response).getValue();
            
            System.out.println("✓ SUCCESS: Connected to ZAP version " + version);
            
            // Test additional endpoints
            try {
                ApiResponse urlsResponse = clientApi.core.urls();
                System.out.println("✓ URLs endpoint accessible");
            } catch (Exception e) {
                System.out.println("✗ URLs endpoint failed: " + e.getMessage());
            }
            
            try {
                ApiResponse scanResponse = clientApi.pscan.recordsToScan();
                System.out.println("✓ Passive scan endpoint accessible");
            } catch (Exception e) {
                System.out.println("✗ Passive scan endpoint failed: " + e.getMessage());
            }
            
        } catch (ClientApiException e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("✗ UNEXPECTED ERROR: " + e.getMessage());
        }
        System.out.println();
    }
}