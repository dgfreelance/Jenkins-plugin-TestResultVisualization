package com.bclc.jenkins.plugins.TestResultDashboard;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.model.Action;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.Run;
import hudson.tasks.junit.ClassResult;
import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.RunList;


public class TestResultDashboardAction extends Actionable implements Action{
	@SuppressWarnings("rawtypes")
	AbstractProject project;

	private LinkedList<Integer> buildNumbers = new LinkedList<Integer>();
	
	public TestResultDashboardAction(@SuppressWarnings("rawtypes") AbstractProject project){
		this.project = project;
	}
	
	@Override
	public String getSearchUrl() {
		return this.hasPermission() ? Constants.URL : null;
	}

	@Override
	public String getIconFileName() {
        return this.hasPermission() ? Constants.ICONFILENAME : null;
	}

	@Override
	public String getDisplayName() {
        return this.hasPermission() ? Constants.NAME : null;
	}

	@Override
	public String getUrlName() {
        return this.hasPermission() ? Constants.URL : null;
	}


    private boolean hasPermission() {
        return project.hasPermission(Item.READ);
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getJsonLoadData() {
		if (isUpdated()) {
			buildNumbers = new LinkedList<Integer>() ;
			RunList<Run> runs = project.getBuilds();
			Iterator<Run> runIterator = runs.iterator();
			while (runIterator.hasNext()) {
				Run run = runIterator.next();
				int buildNumber = run.getNumber();
				buildNumbers.add(buildNumber);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean isUpdated(){
		Iterator<Run> builds = project.getBuilds().iterator();
		int theNumberOfBuilds = 0;
		
		while (builds.hasNext()) {
			Run run = builds.next();
			theNumberOfBuilds+=1;
		}
		return !(buildNumbers.size() == theNumberOfBuilds);
	}
	
	public LinkedList<Integer> getBuilds(){
		return this.buildNumbers;
	}
	
	@SuppressWarnings("rawtypes")
	@JavaScriptMethod
    public JSONArray getBuildSummaryByBuildNumber(int buildNumber) {
		JSONArray jsonArray = new JSONArray();
		Run run = project.getBuildByNumber(buildNumber);
		
		JSONObject json = new JSONObject();
	 	json.put("ProjectName",project.getName());	
	 	json.put("BuildNumber",buildNumber);	
		json.put("DateOfBuild",run.getTimestampString2());
		json.put("BuildDuration",run. getDurationString());
		json.put("BuildDescription",run.getDescription());
		jsonArray.add(json);
		
		
		return jsonArray;
	}
 
    @SuppressWarnings("rawtypes")
	@JavaScriptMethod
    public JSONArray getTestResultByStatusByBuildNumber(int buildNumber) {
		JSONArray jsonArray = new JSONArray();
    	int totalTests = 0, totalFailed = 0, totalPassed = 0;
    	double passedPercent = 0, failedPercent = 0;

    	Run run = project.getBuildByNumber(buildNumber);
      	List<AbstractTestResultAction> testActions = run.getActions(hudson.tasks.test.AbstractTestResultAction.class);
    	
      	if (!testActions.isEmpty()) { //has test result, calculate %
    		for (hudson.tasks.test.AbstractTestResultAction testAction : testActions) {
    			TestResult testResult = (TestResult) testAction.getResult();
    			if (!testResult.isEmpty()) {
        			totalTests += testResult.getTotalCount();
        			totalPassed += testResult.getPassCount();
        			totalFailed += testResult.getFailCount();
        			passedPercent = (totalPassed * 100)/totalTests;
        			failedPercent = (totalFailed * 100)/totalTests;
    			}

    		}
			
		       JSONObject json1 = new JSONObject();
		 	   json1.put("Status","Passed");	
			   json1.put("Value",passedPercent);
			   json1.put("Count",totalPassed);
			   jsonArray.add(json1);
			   
			   JSONObject json2 = new JSONObject();
		 	   json2.put("Status","Failed");	
			   json2.put("Value",failedPercent);
			   json2.put("Count",totalFailed);
			   jsonArray.add(json2);

      	} 	

		return jsonArray;
    }
    
    @SuppressWarnings("rawtypes")
    @JavaScriptMethod
    public JSONArray getFailurePercentageForAreaByBuildNumber(int buildNumber) {
    	JSONArray jsonArray = new JSONArray();
    	
    	Run run = project.getBuildByNumber(buildNumber);
      	List<AbstractTestResultAction> testActions = run.getActions(hudson.tasks.test.AbstractTestResultAction.class);
      	
      	if (!testActions.isEmpty()) { //has test result, calculate %
      		for (hudson.tasks.test.AbstractTestResultAction testAction : testActions) {
    			TestResult testResult = (TestResult) testAction.getResult();
    			Collection<PackageResult> packageResults = testResult.getChildren();
    			
    			for(PackageResult packageResult : packageResults )
    			{
    				String packageName = packageResult.getName();
    				int totalTests = packageResult.getTotalCount();
    				int totalFailed = packageResult.getFailCount();
    				double failurePercentage = (totalFailed * 100)/totalTests;
    				
            		JSONObject json = new JSONObject();
            	 	json.put("PackageName",packageName);	
            		json.put("Value",failurePercentage);
            		json.put("TotalTests",totalTests);
            		json.put("TotalFailed",totalFailed);
            		jsonArray.add(json);
    			}   	
    		}
      	}	

    	return jsonArray;
    }
    
    @SuppressWarnings("rawtypes")
	@JavaScriptMethod
    public JSONArray getTestResultPassTrend() {
    	JSONArray jsonArray = new JSONArray();
    	
    	for (Iterator<Integer> it = buildNumbers.descendingIterator(); it.hasNext(); )
    	{
        	int totalTests = 0, totalPassed = 0;
        	double passedPercent = 0;
        	
        	int buildNumber = it.next().intValue();
        	Run run = project.getBuildByNumber(buildNumber);
          	List<AbstractTestResultAction> testActions = run.getActions(hudson.tasks.test.AbstractTestResultAction.class);
          	
          	if (!testActions.isEmpty()) { //has test result, 
          		for (hudson.tasks.test.AbstractTestResultAction testAction : testActions) {
          			TestResult testResult = (TestResult) testAction.getResult();
          			if(!testResult.isEmpty())
          			{   
          			   totalTests += testResult.getTotalCount();
          			   totalPassed  += testResult.getPassCount();
                 	   passedPercent = (totalPassed * 100)/totalTests;
          			}
          		}

          	}
          	
     		JSONObject json = new JSONObject();
    	 	json.put("BuildNumber",buildNumber);	
    		json.put("Value",passedPercent);
    		jsonArray.add(json);
    	}
    	
    	
    	return jsonArray;
    }
    
    @SuppressWarnings("rawtypes")
	@JavaScriptMethod
    public JSONArray getTestResultForAreaByBuildNumber(int buildNumber) {
    	JSONArray jsonArray = new JSONArray();
    	
    	Run run = project.getBuildByNumber(buildNumber);
      	List<AbstractTestResultAction> testActions = run.getActions(hudson.tasks.test.AbstractTestResultAction.class);
      	
      	if (!testActions.isEmpty()) { //has test result
      		for (hudson.tasks.test.AbstractTestResultAction testAction : testActions) {
    			TestResult testResult = (TestResult) testAction.getResult();
    			Collection<PackageResult> packageResults = testResult.getChildren();
    			
    			for(PackageResult packageResult : packageResults )
    			{
    				String packageName = packageResult.getName();
    				int totalTests = packageResult.getTotalCount();
    				int totalFailed = packageResult.getFailCount();
    				int totalPassed = packageResult.getPassCount();
    				
    				JSONObject json = new JSONObject();
    	    	 	json.put("PackageName",packageName);	
    	    		json.put("TotalTests",totalTests);
    	    		json.put("TotalPassed",totalPassed);
    	    		json.put("TotalFailed",totalFailed);
    	    		jsonArray.add(json);
    			}   	
    		}
      	}
    	
    	return jsonArray;

    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@JavaScriptMethod
    public JSONArray getTestResultFailuresDetailByBuildNumber(int buildNumber) {
    	JSONArray jsonArray = new JSONArray();
    	List<CaseResult> buildFailedTests = new ArrayList<CaseResult>();
    	List<CaseResult> testsOfAllBuild = new ArrayList<CaseResult>();
    	
    	Run build = project.getBuildByNumber(buildNumber);
    	
    	//retrieve failed tests for selected build
    	List<AbstractTestResultAction> buildTestActions = build.getActions(hudson.tasks.test.AbstractTestResultAction.class);
		for (hudson.tasks.test.AbstractTestResultAction testAction : buildTestActions) {
			TestResult testResult = (TestResult) testAction.getResult();
			buildFailedTests = testResult.getFailedTests();
		}
    	
    	//retrieve tests for all build in the past
      	RunList<Run> allRuns = project.getBuilds();
		Iterator<Run> runIterator = allRuns.iterator();
		while (runIterator.hasNext()) {
			Run run = runIterator.next();
			List<AbstractTestResultAction> testActions = run.getActions(hudson.tasks.test.AbstractTestResultAction.class);
      		for (hudson.tasks.test.AbstractTestResultAction testAction : testActions) {
    			TestResult testResult = (TestResult) testAction.getResult();
    			Collection<PackageResult> packageResults = testResult.getChildren();
				for (PackageResult packageResult : packageResults) { 
					for (ClassResult classResult : packageResult.getChildren()) {
						for (CaseResult testCaseResult : classResult.getChildren()) {
							testsOfAllBuild.add(testCaseResult);
						}
					}						
				}  			
      		}
		}
		//count occurrences
		for(CaseResult buildFailedTest : buildFailedTests) {
			String buildFailedTestpackageName = buildFailedTest.getPackageName();
			String buildFailedTesttName = buildFailedTest.getName();
			int passedCount = 0;
			int failedCount =0;
	
			for(CaseResult test : testsOfAllBuild) {
				String testPackageName = test.getPackageName();
				String testName = test.getName();
				if (testPackageName.equals(buildFailedTestpackageName) && testName.equals(buildFailedTesttName) && test.isPassed() )
				{
					passedCount+=1;
				}
				else if (testPackageName.equals(buildFailedTestpackageName) && testName.equals(buildFailedTesttName) && test.isFailed()) {
					failedCount+=1;
				}
			}
						
	  		JSONObject json = new JSONObject();
    	 	json.put("PackageName",buildFailedTest.getPackageName());	
    		json.put("TestName",buildFailedTest.getName());
    		json.put("TotalCount",failedCount+passedCount);
    		json.put("PassCount",passedCount);
    		json.put("FailCount",failedCount);
    		json.put("Age",Math.abs(buildFailedTest.getAge()));
    		json.put("ErrorStackTrace",buildFailedTest.getErrorStackTrace());
    		json.put("ErrorDetails",buildFailedTest.getErrorDetails());
    		json.put("isRegression",buildFailedTest.getStatus()==CaseResult.Status.REGRESSION ? "True" : "False");
    		jsonArray.add(json);
		}
		

		
		
    	return jsonArray;

    }

}
