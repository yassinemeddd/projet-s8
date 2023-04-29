package ma.ac.uir.projets8.service;

import lombok.RequiredArgsConstructor;
import ma.ac.uir.projets8.controller.MeetingController;
import ma.ac.uir.projets8.exception.AccountNotFoundException;
import ma.ac.uir.projets8.exception.MeetingNotFoundException;
import ma.ac.uir.projets8.exception.PageOutOfBoundsException;
import ma.ac.uir.projets8.model.Account;
import ma.ac.uir.projets8.model.Meeting;
import ma.ac.uir.projets8.model.Student;
import ma.ac.uir.projets8.repository.AccountRepository;
import ma.ac.uir.projets8.repository.MeetingRepository;
import ma.ac.uir.projets8.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final AccountRepository accountRepository;

    private final StudentRepository studentRepository;

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    public void addMeeting(@RequestBody MeetingController.NewMeetingRequest request) {
        Meeting meeting = new Meeting();
        meeting.setTitle(request.title());
        meeting.setDate(request.date());
        meeting.setDescription(request.description());
        meeting.setLengthInMinutes(request.lengthInMinutes());
        meeting.setOrganiser(accountRepository.findById(request.organiserId()).orElseThrow(() -> new AccountNotFoundException(request.organiserId())));
        meeting.setParticipants(new HashSet<>(studentRepository.findAllById(request.participantsIds())));
        meetingRepository.save(meeting);
    }

    public Meeting getMeetingById(Integer id) {
        return meetingRepository.findById(id).orElseThrow(() -> new MeetingNotFoundException(id));
    }

    public void updateMeeting(Integer id, MeetingController.NewMeetingRequest request) {
        meetingRepository.findById(id)
                .map(meeting -> {
                            if (!request.title().isEmpty())
                                meeting.setTitle(request.title());
                            if (request.date() != null)
                                meeting.setDate(request.date());
                            if (!request.description().isEmpty())
                                meeting.setDescription(request.description());
                            if (request.lengthInMinutes() != null)
                                meeting.setLengthInMinutes(request.lengthInMinutes());
                            if (request.organiserId() != null)
                                meeting.setOrganiser(accountRepository.findById(request.organiserId()).orElseThrow(() -> new AccountNotFoundException(request.organiserId())));
                            if (!request.participantsIds().isEmpty())
                                meeting.setParticipants(new HashSet<>(studentRepository.findAllById(request.participantsIds())));
                            return meetingRepository.save(meeting);
                        }
                ).orElseThrow(() -> new MeetingNotFoundException(id));;
        //TODO:Add case of recieving an invalid id
    }

    public List<Student> getMeetingParticipants(Integer id) {
        return new ArrayList<>(meetingRepository.findById(id).orElseThrow(() -> new MeetingNotFoundException(id)).getParticipants());
    }

    public Account getMeetingOrganiser(Integer id) {
        return meetingRepository.findById(id).orElseThrow(() -> new MeetingNotFoundException(id)).getOrganiser();
    }
    public void deleteMeeting(Integer id) {

        meetingRepository.deleteById(id);
    }

    public ResponseEntity<List<Meeting>> getMeetingsPage(int pageNumber, int size){
        Page<Meeting> resultPage = meetingRepository.findAll(PageRequest.of(pageNumber, size));
        if (pageNumber > resultPage.getTotalPages()) {
            throw new PageOutOfBoundsException(pageNumber);
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-pages", String.valueOf(resultPage.getTotalPages()));
        return new ResponseEntity<>(resultPage.getContent(), responseHeaders, HttpStatus.OK);
    }


}
