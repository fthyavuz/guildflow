import { Pipe, PipeTransform } from '@angular/core';
import { EventParticipantResponse } from '../models/event.model';

@Pipe({
    name: 'filterGoing',
    standalone: true
})
export class FilterGoingPipe implements PipeTransform {
    transform(participants: EventParticipantResponse[] | undefined): EventParticipantResponse[] {
        if (!participants) return [];
        return participants.filter(p => p.isGoing === true);
    }
}

@Pipe({
    name: 'filterNotGoing',
    standalone: true
})
export class FilterNotGoingPipe implements PipeTransform {
    transform(participants: EventParticipantResponse[] | undefined): EventParticipantResponse[] {
        if (!participants) return [];
        return participants.filter(p => p.isGoing === false);
    }
}
