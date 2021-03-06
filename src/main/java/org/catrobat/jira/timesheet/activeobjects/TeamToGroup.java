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

package org.catrobat.jira.timesheet.activeobjects;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload
@Table("TeamToGroup")
public interface TeamToGroup extends Entity {

    /*  -------------------------------------------
        | TeamToGroup |
        -------------------------------------------
        | Team | Group | Role |
        -------------------------------------------
        Team : Group (m:m)
        Role = attribute of the relationship, no part of Team or Group
     */

    @NotNull
    Team getTeam();
    void setTeam(Team team);

    @NotNull
    Group getGroup();
    void setGroup(Group group);

    //further information about the relationship
    @NotNull
    Role getRole();
    void setRole(Role role);

    enum Role {
        COORDINATOR, DEVELOPER
    }
}