package utils;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.Proxy;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

public class ZapUtil1 {
	public static ClientApi clientApi;
	public static Proxy proxy;
	public static final String zapAddress = "127.0.0.1";
	public static final int zapPort = 8080;
	public static final String apiKey = "5uu4p51pmdk0vkmovk2nfad8v3";
	
	static {
		try {
			clientApi = new ClientApi(zapAddress, zapPort, apiKey);
			proxy = new Proxy().setHttpProxy(zapAddress + ":" + zapPort)
					.setSslProxy(zapAddress + ":" + zapPort);
			
			// Test ZAP connection
			verifyZapConnection();
			System.out.println("ZAP Client initialized and connected successfully");
		} catch (Exception e) {
			System.err.println("Failed to initialize ZAP Client: " + e.getMessage());
			System.err.println("Please ensure OWASP ZAP is running on " + zapAddress + ":" + zapPort);
			e.printStackTrace();
		}
	}
	
	public static void verifyZapConnection() throws ClientApiException {
		int maxRetries = 3;
		int retryDelay = 2000; // 2 seconds
		
		for (int i = 0; i < maxRetries; i++) {
			try {
				// Test connection by getting ZAP version
				ApiResponse response = clientApi.core.version();
				String version = ((ApiResponseElement) response).getValue();
				System.out.println("Connected to ZAP version: " + version);
				return; // Connection successful
			} catch (ClientApiException e) {
				System.err.println("Connection attempt " + (i + 1) + " failed: " + e.getMessage());
				if (i < maxRetries - 1) {
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new ClientApiException("Connection verification interrupted", ie);
					}
				} else {
					throw new ClientApiException("Failed to connect to ZAP after " + maxRetries + " attempts", e);
				}
			}
		}
	}
	
	public static void waitTillPassiveScanCompleted() throws InterruptedException {
		int maxRetries = 3;
		int retryDelay = 5000; // 5 seconds
		
		for (int attempt = 0; attempt < maxRetries; attempt++) {
			try {
				ApiResponse apiResponse = clientApi.pscan.recordsToScan();
				String tempVal = ((ApiResponseElement) apiResponse).getValue();
				System.out.println("Initial records to scan: " + tempVal);
				
				while (!tempVal.equals("0")) {
					System.out.println("Passive scan is in process. Records remaining: " + tempVal);
					Thread.sleep(3000);
					apiResponse = clientApi.pscan.recordsToScan();
					tempVal = ((ApiResponseElement) apiResponse).getValue();
				}
				System.out.println("Passive scan is completed");
				return; // Success
				
			} catch (ClientApiException e) {
				System.err.println("Passive scan attempt " + (attempt + 1) + " failed: " + e.getMessage());
				if (attempt < maxRetries - 1) {
					System.out.println("Retrying in " + (retryDelay / 1000) + " seconds...");
					Thread.sleep(retryDelay);
				} else {
					System.err.println("Passive scan failed after " + maxRetries + " attempts");
					throw new RuntimeException("Passive scan failed", e);
				}
			}
		}
	}
	
	public static void addURLToScanTree(String site_to_test) throws ClientApiException {
		if (site_to_test == null || site_to_test.trim().isEmpty()) {
			throw new IllegalArgumentException("Site URL cannot be null or empty");
		}
		
		int maxRetries = 3;
		int retryDelay = 3000; // 3 seconds
		
		for (int attempt = 0; attempt < maxRetries; attempt++) {
			try {
				clientApi.core.accessUrl(site_to_test, "false");
				Thread.sleep(2000); // Give time for URL to be added
				
				if (getUrlsFromScanTree().contains(site_to_test)) {
					System.out.println(site_to_test + " has been added to scan tree");
					return; // Success
				} else {
					throw new RuntimeException(site_to_test + " not added to scan tree");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Thread interrupted while adding URL to scan tree", e);
			} catch (ClientApiException e) {
				System.err.println("Add URL attempt " + (attempt + 1) + " failed: " + e.getMessage());
				if (attempt < maxRetries - 1) {
					System.out.println("Retrying in " + (retryDelay / 1000) + " seconds...");
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new RuntimeException("Thread interrupted during retry", ie);
					}
				} else {
					throw new ClientApiException("Failed to add URL after " + maxRetries + " attempts", e);
				}
			}
		}
	}
	
	public static List<String> getUrlsFromScanTree() throws ClientApiException {
		int maxRetries = 3;
		int retryDelay = 2000; // 2 seconds
		
		for (int attempt = 0; attempt < maxRetries; attempt++) {
			try {
				ApiResponse apiResponse = clientApi.core.urls();
				List<ApiResponse> responses = ((ApiResponseList) apiResponse).getItems();
				
				return responses.stream()
						.map(r -> ((ApiResponseElement) r).getValue())
						.collect(Collectors.toList());
			} catch (ClientApiException e) {
				System.err.println("Get URLs attempt " + (attempt + 1) + " failed: " + e.getMessage());
				if (attempt < maxRetries - 1) {
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new ClientApiException("Thread interrupted during retry", ie);
					}
				} else {
					throw new ClientApiException("Failed to get URLs after " + maxRetries + " attempts", e);
				}
			}
		}
		return java.util.Collections.emptyList(); // Should never reach here
	}
	
	public static void performActivescan(String site_to_test) throws ClientApiException {
		if (site_to_test == null || site_to_test.trim().isEmpty()) {
			throw new IllegalArgumentException("Site URL cannot be null or empty");
		}
		
		String url = site_to_test;
		String recurse = "true";
		String inscopeonly = null;
		String scanpolicyname = null;
		String method = null;
		String postdata = null;
		Integer contextid = 0;
		
		try {
			ApiResponse scanResponse = clientApi.ascan.scan(url, recurse, inscopeonly, 
					scanpolicyname, method, postdata, contextid);
			String scanid = ((ApiResponseElement) scanResponse).getValue();
			System.out.println("Active scan started with ID: " + scanid);
			waitTillActiveScanCompleted(scanid);
		} catch (Exception e) {
			System.err.println("Error during active scan: " + e.getMessage());
			throw new ClientApiException("Active scan failed", e);
		}
	}
	
	public static void waitTillActiveScanCompleted(String scanid) throws ClientApiException {
		if (scanid == null || scanid.trim().isEmpty()) {
			throw new IllegalArgumentException("Scan ID cannot be null or empty");
		}
		
		try {
			ApiResponse apiResponse = clientApi.ascan.status(scanid);
			String status = ((ApiResponseElement) apiResponse).getValue();
			
			System.out.println("Active scan progress: " + status + "%");
			
			while (!status.equals("100")) {
				Thread.sleep(5000); // Increased sleep time to reduce API calls
				apiResponse = clientApi.ascan.status(scanid);
				status = ((ApiResponseElement) apiResponse).getValue();
				System.out.println("Active scan progress: " + status + "%");
			}
			System.out.println("Active scan has completed");
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted during active scan", e);
		}
	}
	
	public static void generateZapReport(String site_to_test) {
		String title = "ZAP P2DASH";
		String template = "modern";
		String theme = "skyline";
		String description = "Security scan results";
		String contexts = null;
		String sites = site_to_test;
		String sections = null;
		String includedconfidences = "high|medium|low";
		String includedrisks = "high|medium|low"; // Include all risks
		String reportfilename = "{{yyyy-MM-dd}}-ZAP-Report-[[site]]";
		String reportfilenamepattern = "";
		String reportdir = System.getProperty("user.dir") + "/reports";
		String display = null;
		
		int maxRetries = 3;
		int retryDelay = 5000; // 5 seconds
		
		for (int attempt = 0; attempt < maxRetries; attempt++) {
			try {
				// Create reports directory if it doesn't exist
				java.io.File reportsDir = new java.io.File(reportdir);
				if (!reportsDir.exists()) {
					reportsDir.mkdirs();
					System.out.println("Created reports directory: " + reportdir);
				}
				
				
				clientApi.reports.generate(title, template, theme, description, contexts, 
						sites, sections, includedconfidences, includedrisks, reportfilename, 
						reportfilenamepattern, reportdir, display);
				System.out.println("Report generated successfully at: " + reportdir + "/" + reportfilename);
				return; // Success
				
			} catch (ClientApiException e) {
				System.err.println("Report generation attempt " + (attempt + 1) + " failed: " + e.getMessage());
				if (attempt < maxRetries - 1) {
					System.out.println("Retrying report generation in " + (retryDelay / 1000) + " seconds...");
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						System.err.println("Report generation interrupted");
						return;
					}
				} else {
					System.err.println("Report generation failed after " + maxRetries + " attempts");
					e.printStackTrace();
				}
			}
		}
	}
}