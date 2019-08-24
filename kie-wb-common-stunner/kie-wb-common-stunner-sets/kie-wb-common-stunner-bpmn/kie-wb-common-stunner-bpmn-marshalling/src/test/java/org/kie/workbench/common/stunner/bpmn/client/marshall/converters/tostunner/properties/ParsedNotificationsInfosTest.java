/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.client.marshall.converters.tostunner.properties;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.client.marshall.converters.customproperties.ParsedNotificationsInfos;
import org.kie.workbench.common.stunner.bpmn.client.marshall.converters.fromstunner.associations.AssociationType;
import org.kie.workbench.common.stunner.bpmn.definition.property.notification.NotificationValue;

import static org.junit.Assert.assertEquals;

public class ParsedNotificationsInfosTest {

    @Test
    public void testNotification() {
        String body = "[from:director|tousers:director,jack,katy|togroups:Forms,IT|replyTo:guest|subject:asd|body:asd]@[11h]";
        NotificationValue actual = ParsedNotificationsInfos.of(AssociationType.NOT_COMPLETED_NOTIFY.getName(), body);
        NotificationValue expected = new NotificationValue();
        expected.setType(AssociationType.NOT_COMPLETED_NOTIFY.getName());
        expected.setFrom("director");
        expected.setReplyTo("guest");
        expected.setSubject("asd");
        expected.setBody("asd");
        expected.setExpiresAt("11h");
        expected.setGroups(new ArrayList<>(Arrays.asList("Forms", "IT")));
        expected.setUsers(new ArrayList<>(Arrays.asList("director", "jack", "katy")));

        assertEquals(expected, actual);
    }

    @Test
    public void testNotificationPartial() {
        String body = "[from:|tousers:|togroups:|replyTo:|subject:|body:]@[0h]]";
        NotificationValue actual = ParsedNotificationsInfos.of(AssociationType.NOT_COMPLETED_NOTIFY.getName(), body);
        NotificationValue expected = new NotificationValue();
        expected.setType(AssociationType.NOT_COMPLETED_NOTIFY.getName());
        expected.setExpiresAt("0h");

        assertEquals(expected.toString(), actual.toString());
        assertEquals(expected.toCDATAFormat(), actual.toCDATAFormat());
        assertEquals(expected, actual);
    }

    @Test
    public void testNotificationEmpty() {
        NotificationValue value = ParsedNotificationsInfos.of(AssociationType.NOT_COMPLETED_NOTIFY.getName(), "");
        NotificationValue valid = new NotificationValue();
        valid.setType(AssociationType.NOT_COMPLETED_NOTIFY.getName());
        assertEquals(valid, value);
    }
}
