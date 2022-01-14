insert into guest(id, name) values(null, 'Roger Federer');
insert into guest(id, name) values(null, 'Rafael Nadal');

insert into tennis_court(id, name) values(null, 'Roland Garros - Court Philippe-Chatrier');

insert into schedule (id, start_date_time, end_date_time, tennis_court_id)           --schedule in the future having no reservation
    values (null, '2025-12-20T20:00:00.0', '2025-02-20T21:00:00.0', 1);

insert into schedule (id, start_date_time, end_date_time, tennis_court_id)           --schedule in the future having 3 reservations in all the 3 possible statuses
    values (null, '2030-12-20T20:00:00.0', '2030-02-20T21:00:00.0', 1);

insert into schedule (id, start_date_time, end_date_time, tennis_court_id)           --schedule in the past
    values (null, '2000-12-20T20:00:00.0', '2000-02-20T21:00:00.0', 1);

insert into reservation (id, value, reservation_status, refund_value, guest_id, schedule_id)
    values (null, 10, 0, 10, 1, 2);

insert into reservation (id, value, reservation_status, refund_value, guest_id, schedule_id)
    values (null, 10, 1, 10, 1, 2);

insert into reservation (id, value, reservation_status, refund_value, guest_id, schedule_id)
    values (null, 10, 2, 10, 1, 2);