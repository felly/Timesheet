/*
 * Copyright 2016 Adrian Schnedlitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.catrobat.jira.timesheet.rest;


import org.catrobat.jira.timesheet.services.ConfigService;
import org.catrobat.jira.timesheet.activeobjects.Scheduling;
import org.catrobat.jira.timesheet.rest.json.JsonScheduling;
import org.catrobat.jira.timesheet.scheduling.ActivityNotificationJob;
import org.catrobat.jira.timesheet.scheduling.ActivityVerificationJob;
import org.catrobat.jira.timesheet.scheduling.OutOfTimeJob;
import org.catrobat.jira.timesheet.scheduling.TimesheetScheduler;
import org.catrobat.jira.timesheet.services.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

@Path("/scheduling")
@Produces({MediaType.APPLICATION_JSON})
public class SchedulingRest {
    private final ConfigService configService;
    private final PermissionService permissionService;
    private final TimesheetEntryService entryService;
    private final TimesheetService sheetService;
    private final TeamService teamService;
    private final CategoryService categoryService;
    private final TimesheetScheduler timesheetScheduler;
    private final SchedulingService schedulingService;
    private final org.apache.log4j.Logger LOGGER = org.apache.log4j.LogManager.getLogger(SchedulingRest.class);

    public SchedulingRest(final ConfigService configService, final PermissionService permissionService,
            final TimesheetEntryService entryService, final TimesheetService sheetService, TeamService teamService,
            CategoryService categoryService, TimesheetScheduler timesheetScheduler, SchedulingService schedulingService) {
        this.configService = configService;
        this.permissionService = permissionService;
        this.entryService = entryService;
        this.sheetService = sheetService;
        this.teamService = teamService;
        this.categoryService = categoryService;
        this.timesheetScheduler = timesheetScheduler;
        this.schedulingService = schedulingService;
    }

    @GET
    @Path("/trigger/activity/notification")
    public Response activityNotification(@Context HttpServletRequest request) {
        Response unauthorized = permissionService.checkRootPermission();
        if (unauthorized != null) {
            return unauthorized;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("sheetService", sheetService);
        params.put("entryService", entryService);
        params.put("teamService", teamService);
        params.put("configService", configService);
        params.put("schedulingService", schedulingService);

        ActivityNotificationJob activityNotificationJob = new ActivityNotificationJob();
        activityNotificationJob.execute(params);

        return Response.noContent().build();
    }

    @GET
    @Path("/trigger/activity/verification")
    public Response activityVerification(@Context HttpServletRequest request) {
        LOGGER.error("activity verification triggerd");

        Response unauthorized = permissionService.checkRootPermission();
        if (unauthorized != null) {
            return unauthorized;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("sheetService", sheetService);
        params.put("entryService", entryService);
        params.put("teamService", teamService);
        params.put("categoryService", categoryService);
        params.put("schedulingService", schedulingService);

        ActivityVerificationJob activityVerificationJob = new ActivityVerificationJob();
        activityVerificationJob.execute(params);

        return Response.noContent().build();
    }

    @GET
    @Path("/trigger/out/of/time/notification")
    public Response outOfTimeNotification(@Context HttpServletRequest request) {
        Response unauthorized = permissionService.checkRootPermission();
        if (unauthorized != null) {
            return unauthorized;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("sheetService", sheetService);
        params.put("configService", configService);
        params.put("schedulingService", schedulingService);

        OutOfTimeJob outOfTimeJob = new OutOfTimeJob();
        outOfTimeJob.execute(params);

        return Response.noContent().build();
    }

    @GET
    @Path("/getScheduling")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduling(@Context HttpServletRequest request) {
        Response unauthorized = permissionService.checkRootPermission();
        if (unauthorized != null) {
            return unauthorized;
        }

        Scheduling scheduling = schedulingService.getScheduling();
        JsonScheduling jsonScheduling = new JsonScheduling(scheduling);

        return Response.ok(jsonScheduling).build();
    }

    @PUT
    @Path("/saveScheduling")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setScheduling(final JsonScheduling jsonScheduling, @Context HttpServletRequest request) {
        Response unauthorized = permissionService.checkRootPermission();
        if (unauthorized != null) {
            return unauthorized;
        }

        schedulingService.setScheduling(jsonScheduling.getInactiveTime(),
                jsonScheduling.getOfflineTime(), jsonScheduling.getRemainingTime(), jsonScheduling.getOutOfTime());
        //timesheetScheduler.reschedule(); // since parameters will be updated on the fly, no rescheduling needed

        return Response.noContent().build();
    }

    @POST
    @Path("/saveMaxModificationDays/{maxModDays}")
    public Response saveMaxModificationDays(@Context HttpServletRequest request, final @PathParam("maxModDays") int max_mod_days){
        try {
            schedulingService.setMaxModificationDays(max_mod_days);
        }catch (Exception e){
           return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/getMaxModificationDays")
    public Response getMaxModificationDays(@Context HttpServletRequest request){
        try{
            return Response.ok().entity(schedulingService.getMaxModificationDays()).build();
        }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
