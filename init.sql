INSERT INTO client (guid, agency, firstName, lastName, status, dob, createdDateTime) VALUES
                                                                                         ('client1', 'AgencyA', 'John', 'Doe', 'Active', '1990-01-01', '2023-10-01T10:00:00'),
                                                                                         ('client2', 'AgencyB', 'Jane', 'Smith', 'Active', '1985-05-15', '2023-10-02T11:00:00'),
                                                                                         ('client3', 'AgencyA', 'Bob', 'Brown', 'Active', '1992-08-20', '2023-10-03T12:00:00'),
                                                                                         ('client4', 'AgencyC', 'Alice', 'Johnson', 'Active', '1988-12-10', '2023-10-04T13:00:00'),
                                                                                         ('client5', 'AgencyB', 'Charlie', 'Davis', 'Active', '1995-03-25', '2023-10-05T14:00:00');

INSERT INTO old_notes (guid, comments, modifiedDateTime, clientGuid, dateTime, loggedUser, createdDateTime) VALUES
                                                                                                                ('note1', 'First note for John Doe', '2023-10-01T10:05:00', 'client1', '2023-10-01T10:05:00', 'user1', '2023-10-01T10:05:00'),
                                                                                                                ('note2', 'Second note for John Doe', '2023-10-01T11:00:00', 'client1', '2023-10-01T11:00:00', 'user1', '2023-10-01T11:00:00'),
                                                                                                                ('note3', 'First note for Jane Smith', '2023-10-02T12:00:00', 'client2', '2023-10-02T12:00:00', 'user2', '2023-10-02T12:00:00'),
                                                                                                                ('note4', 'Second note for Jane Smith', '2023-10-02T13:00:00', 'client2', '2023-10-02T13:00:00', 'user2', '2023-10-02T13:00:00'),
                                                                                                                ('note5', 'First note for Bob Brown', '2023-10-03T14:00:00', 'client3', '2023-10-03T14:00:00', 'user3', '2023-10-03T14:00:00'),
                                                                                                                ('note6', 'Second note for Bob Brown', '2023-10-03T15:00:00', 'client3', '2023-10-03T15:00:00', 'user3', '2023-10-03T15:00:00'),
                                                                                                                ('note7', 'First note for Alice Johnson', '2023-10-04T16:00:00', 'client4', '2023-10-04T16:00:00', 'user4', '2023-10-04T16:00:00'),
                                                                                                                ('note8', 'Second note for Alice Johnson', '2023-10-04T17:00:00', 'client4', '2023-10-04T17:00:00', 'user4', '2023-10-04T17:00:00'),
                                                                                                                ('note9', 'First note for Charlie Davis', '2023-10-05T18:00:00', 'client5', '2023-10-05T18:00:00', 'user5', '2023-10-05T18:00:00'),
                                                                                                                ('note10', 'Second note for Charlie Davis', '2023-10-05T19:00:00', 'client5', '2023-10-05T19:00:00', 'user5', '2023-10-05T19:00:00');
