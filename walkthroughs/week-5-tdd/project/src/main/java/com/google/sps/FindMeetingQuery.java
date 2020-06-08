// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.*;
import java.lang.*;

public final class FindMeetingQuery {
  /**
   * Returns the times when the meeting could happen on a certain day given meeting request.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> ret = new ArrayList<TimeRange>();

    
    Set<String> attendees =  new HashSet<String>();
    attendees.addAll(request.getAttendees()); 
    int reqDuration = (int) request.getDuration();

    //if requested duration is longer than 24hrs, there is no time slot to accommodate that
    if (reqDuration > 24*60){
      return ret;
    }

    //if there are no scheduled events, the entire day is free
    if (events.isEmpty()){
      ret.add(TimeRange.WHOLE_DAY);
      return ret;
    }

    //accumulates an arraylist of all times that it is not possible to meet (blocked time)
    //a time is blocked if there are attendees in the meeting request and in the event
    List<TimeRange> notPossible= new ArrayList<TimeRange>();
    for (Event event : events){
      Set<String> eventAttendees = event.getAttendees();
      Set<String> overlap = new HashSet<>(eventAttendees);
      overlap.retainAll(attendees);

      if (!overlap.isEmpty()){
        notPossible.add(event.getWhen());
      }
    }

    //if there are no blocked times, the entire day is free
    if (notPossible.isEmpty()){
      ret.add(TimeRange.WHOLE_DAY);
      return ret;
    }

    //combines overlapping blocked times into a consolidated arraylist
    Collections.sort(notPossible, TimeRange.ORDER_BY_START);
    TimeRange[] blockedTimes = ((List<TimeRange>) notPossible).toArray(new TimeRange[notPossible.size()]); 
    List<TimeRange> tmp = new ArrayList<TimeRange>();
    tmp.add(blockedTimes[0]);
    int tmpIdx = 0;

    for (int i=0; i < blockedTimes.length; i++){
      if (blockedTimes[i].overlaps(tmp.get(tmpIdx))){
        tmp.set(tmpIdx, 
                TimeRange.fromStartEnd(Math.min(tmp.get(tmpIdx).start(), blockedTimes[i].start()), 
                                       Math.max(tmp.get(tmpIdx).end(), blockedTimes[i].end()) - 1, 
                                       true));
      }

      else {
        tmpIdx += 1;
        tmp.add(blockedTimes[i]);
      }
    }

    //finds all available (free) times between blocked time that are of requested duration or longer
    TimeRange[] findFree = ((List<TimeRange>) tmp).toArray(new TimeRange[tmp.size()]); 
    if (TimeRange.fromStartEnd(0, findFree[0].start(), true).duration()>=reqDuration){
      ret.add(TimeRange.fromStartEnd(0, findFree[0].start() - 1, true));
    }

    for (int i=0; i < findFree.length-1; i++){
      if (TimeRange.fromStartEnd(findFree[i].end(), findFree[i+1].start(), true).duration() >= reqDuration){
        ret.add(TimeRange.fromStartEnd(findFree[i].end(), findFree[i+1].start() - 1, true));
      }
    }

    if (TimeRange.fromStartEnd(findFree[findFree.length - 1].end(), 24*60, true).duration() >= reqDuration){
      ret.add(TimeRange.fromStartEnd(findFree[findFree.length - 1].end(), (24*60) - 1, true));
    }
    
    return ret;
  }   
}