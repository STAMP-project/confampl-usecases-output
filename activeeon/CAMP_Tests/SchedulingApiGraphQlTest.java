/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.systemtests.test.rest.graphql;

import static java.lang.Math.toIntExact;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.systemtests.helper.scheduler.AbstractSchedulerRestTest;
import org.ow2.proactive.systemtests.helper.scheduler.SystemTestGraphQlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author ActiveEon Team
 * @since 2019-01-18
 */
public class SchedulingApiGraphQlTest extends AbstractSchedulerRestTest {

    @Autowired
    SystemTestGraphQlJobHelper helper;

    @Before
    public void setUp() {
        systemTestRestJobHelper.killAndDeleteAllSubmittedJobs(adminAndUserGroupSessionId);
        submitAndGetJobId("systemtests/workflows/catalog/simple_task_with_catalog_GI.xml");
        submitAndGetJobId("systemtests/workflows/graphql/simple_workflow_with_variables.xml");
        submitAndGetJobId("systemtests/workflows/graphql/another_simple_workflow_with_variables.xml");
    }

    @Test
    public void testFetchingAllJobs() throws ParseException {
        JSONObject res = helper.query("{ jobs { totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        assertThat(toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount")), is(3));
    }

    @Test
    public void testFetchingOneJobWithSpecificVariable() throws ParseException {
        JSONObject res = helper.query("{ jobs " + "(filter: [{variables: {key: \"cmd\", value: \"%Toto%\"}}]) " +
                                      "{ totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        int nbJobs = toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount"));
        String jobName = (String) helper.getField(res, "data", "jobs", "edges", 0, "node", "name");
        assertThat(nbJobs, is(1));
        assertThat(jobName, is("echo-workflow"));
    }

    @Test
    public void testFetchingJobsByCmdVariableNameAndValue() throws ParseException {
        JSONObject res = helper.query("{ jobs " + "(filter: {variables: [{key: \"cmd\"}, {value: \"%Toto%\"}]}) " +
                                      "{ totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        int nbJobs = toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount"));
        assertThat(nbJobs, is(2));
    }

    @Test
    public void testFetchingOneJobWithComplexVariablesExpression() throws ParseException {
        JSONObject res = helper.query("{ jobs " +
                                      "(filter: [{variables: [{key: \"cmd\", value: \"echo%\"}, {key: \"nickname\", value: \"%\"}]}]) " +
                                      "{ totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        int nbJobs = toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount"));
        String jobName = (String) helper.getField(res, "data", "jobs", "edges", 0, "node", "name");
        assertThat(nbJobs, is(1));
        assertThat(jobName, is("echo-workflow"));
    }

    @Test
    public void testFetchingBothCmdJobsWithVariables() throws ParseException {
        JSONObject res = helper.query("{ jobs " + "(filter: [{variables: {key: \"cmd\", value: \"echo%\"}}, " +
                                      "          {variables: {key: \"%\", value: \"Toto\"}}]) " +
                                      "{ totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        int nbJobs = toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount"));
        assertThat(nbJobs, is(2));
    }

    @Test
    public void testFetchingAllJobsWithExplicitFilterExpression() throws ParseException {
        JSONObject res = helper.query("{ jobs " +
                                      "(filter: [{variables: [{key: \"cmd\", value: \"%\"}, {key: \"nickname\", value: \"Toto\"}]}," +
                                      "          {variables: {key: \"nickname\", value: \"%\"}}," +
                                      "          {name: \"*simple_task*\"}]) " +
                                      "{ totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        int nbJobs = toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount"));
        assertThat(nbJobs, is(3));
    }

    @Test
    public void testFetchingJobsByCmdVariableName() throws ParseException {
        JSONObject res = helper.query("{ jobs " + "(filter: {variables: {key: \"cmd\"}}) " +
                                      "{ totalCount edges { node { id name } } } }", adminAndUserGroupSessionId);
        int nbJobs = toIntExact((Long) helper.getField(res, "data", "jobs", "totalCount"));
        assertThat(nbJobs, is(2));
    }
}
