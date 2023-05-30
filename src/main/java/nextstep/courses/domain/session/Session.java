package nextstep.courses.domain.session;

import exception.LmsException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import nextstep.courses.domain.Course;
import nextstep.courses.domain.session.student.SessionStudent;
import nextstep.courses.domain.session.student.SessionStudents;
import nextstep.courses.domain.session.teacher.SessionTeacher;
import nextstep.courses.domain.session.teacher.SessionTeachers;
import nextstep.courses.exception.SessionExceptionCode;
import nextstep.users.domain.NsUser;

public class Session {
  private final Long id;
  private Long courseId;
  private SessionPayType sessionPayType;
  private SessionStatus sessionStatus;
  private SessionCapacity capacity;
  private SessionPeriod sessionPeriod;

  private SessionStudents students = new SessionStudents(new ArrayList<>());
  private SessionTeachers teachers = new SessionTeachers(new ArrayList<>());

  public Session(
      Long id, Long courseId, SessionPayType sessionPayType,
      SessionProgressStatus sessionProgressStatus, SessionRecruitStatus sessionRecruitStatus,
      int maxPersonnelCount, LocalDateTime startAt, LocalDateTime finishAt
  ) {
    this.id = id;
    this.sessionPayType = sessionPayType;
    this.capacity = new SessionCapacity(maxPersonnelCount);
    this.courseId = courseId;
    this.sessionPeriod = new SessionPeriod(startAt, finishAt);
    this.sessionStatus = new SessionStatus(sessionProgressStatus, sessionRecruitStatus);
  }

  public Session(
      Long id, Course course, SessionPayType payType, SessionProgressStatus sessionProgressStatus, SessionRecruitStatus sessionRecruitStatus,
      int maxPersonnelCount, LocalDateTime startAt, LocalDateTime finishAt
  ) {
    this(id, course.getId(), payType, sessionProgressStatus, sessionRecruitStatus, maxPersonnelCount, startAt, finishAt);
  }

  public Session (Session session) {
    this.id = session.id;
    this.sessionPayType = session.sessionPayType;
    this.capacity = session.capacity;
    this.courseId = session.courseId;
    this.sessionPeriod = session.sessionPeriod;
    this.sessionStatus = session.sessionStatus;
  }

  public Session (Session session, SessionStudents students) {
    this(session);
    this.students = students;
  }

  public Session (Session session, SessionStudents students, SessionTeachers teachers) {
    this(session, students);
    this.teachers = teachers;
  }

  public SessionStudent addPersonnel(NsUser nsUser) {
    if (this.teachers.contains(nsUser)) {
      throw new LmsException(SessionExceptionCode.TEACHER_CANNOT_ENROLL_SESSION);
    }

    if (this.sessionStatus.isNotEnrollable()) {
      throw new LmsException(SessionExceptionCode.CANNOT_ENROLL_SESSION);
    }

    if (this.cannotAcceptMoreStudent()) {
      throw new LmsException(SessionExceptionCode.EXCEED_MAX_PERSONNEL_COUNT);
    }

    SessionStudent student = new SessionStudent(this, nsUser.getId());
    students.addStudent(student);
    return student;
  }

  public int getMaxCapacity() {
    return this.capacity.getMaxCapacity();
  }

  public Long getId() {
    return id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public String getSessionPayTypeName() {
    return sessionPayType.name();
  }

  public String getProgressStatusName() {
    return sessionStatus.getProgressStatusName();
  }

  public String getRecruitStatusName() {
    return sessionStatus.getRecruitStatusName();
  }

  public SessionPeriod getSessionPeriod() {
    return sessionPeriod;
  }

  public long getCurrentStudentSize() {
    return students.getCurrentStudentCount();
  }

  public boolean canAcceptMoreStudent() {
    return this.getMaxCapacity() > this.getCurrentStudentSize();
  }

  public boolean cannotAcceptMoreStudent() {
    return !this.canAcceptMoreStudent();
  }

  public SessionStudent getStudent(Long nsUserId) {
    return this.students.getStudent(nsUserId);
  }

  public SessionTeacher getTeacher(Long nsUserId) {
    return this.teachers.getTeacher(nsUserId);
  }

  public SessionStudent cancelStudent(Long nsUserId) {
    SessionStudent student = this.students.getStudent(nsUserId);
    if (student == null) {
      throw new LmsException(SessionExceptionCode.STUDENT_NOT_FOUND);
    }

    return student;
  }

  @Deprecated
  public boolean isLegacy() {
    return this.sessionStatus.isLegacy();
  }
}
