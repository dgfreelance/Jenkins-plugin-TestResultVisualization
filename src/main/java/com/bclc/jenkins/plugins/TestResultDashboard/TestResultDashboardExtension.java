package com.bclc.jenkins.plugins.TestResultDashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;

@Extension
public class TestResultDashboardExtension  extends TransientProjectActionFactory{

	@Override
	public Collection<? extends Action> createFor(@SuppressWarnings("rawtypes") AbstractProject target) {
		
		final List<TestResultDashboardAction> projectActions = target
                .getActions(TestResultDashboardAction.class);
        final ArrayList<Action> actions = new ArrayList<Action>();
        if (projectActions.isEmpty()) {
            final TestResultDashboardAction newAction = new TestResultDashboardAction(target);
            actions.add(newAction);
            return actions;
        } else {
            return projectActions;
        }
	}


}
