-- INSERT INTO 'calendars' table
INSERT INTO calendars (cal_idx, c_holiday, c_theme) VALUES
(1, '2024-01-01', 'LIGHT'),
(2, '2024-01-02', 'LIGHT'),
(3, '2024-01-03', 'LIGHT'),
(4, '2024-01-04', 'LIGHT'),
(5, '2024-01-05', 'LIGHT');

-- user 테이블에 대한 더미 데이터 삽입
INSERT INTO user (u_idx, u_email, u_password, u_nickname, u_joinday, cal_idx, provider) VALUES
(1, 'johndoe@example.com', '$2a$10$G1ftYEFC6HTesuwRy9Z6v.kq5KSHLphPOVCxx3NBnnmQ03sgDuVFW', 'JohnDoe', '2024-01-01 09:00:00', 1, 'LOCAL'),
(2, 'janesmith@example.com', '$2a$10$G1ftYEFC6HTesuwRy9Z6v.kq5KSHLphPOVCxx3NBnnmQ03sgDuVFW', 'JaneSmith', '2024-01-02 10:00:00', 2, 'LOCAL'),
(3, 'mikejohnson@example.com', '$2a$10$G1ftYEFC6HTesuwRy9Z6v.kq5KSHLphPOVCxx3NBnnmQ03sgDuVFW', 'MikeJohnson', '2024-01-03 11:00:00', 3, 'LOCAL'),
(4, 'alicewong@example.com', '$2a$10$G1ftYEFC6HTesuwRy9Z6v.kq5KSHLphPOVCxx3NBnnmQ03sgDuVFW', 'AliceWong', '2024-01-04 12:00:00', 4, 'LOCAL'),
(5, 'davidlee@example.com', '$2a$10$G1ftYEFC6HTesuwRy9Z6v.kq5KSHLphPOVCxx3NBnnmQ03sgDuVFW', 'DavidLee', '2024-01-05 13:00:00', 5, 'LOCAL');

-- INSERT INTO 'diary' table
INSERT INTO diary (d_idx, d_title, d_content, d_date, d_category, cal_idx) VALUES
(1, 'Day 1', 'Went for a walk', '2024-01-01', 'DAILY', 1),
(6, 'Day 1', 'Went for a walk', '2024-11-01', 'GROWTH', 1),
(7, 'Day 1', 'Went for a walk', '2024-11-05', 'GROWTH', 1),
(8, 'Day 1', 'Went for a walk', '2024-11-05', 'EXERCISE', 1),
(9, 'Day 1', 'Went for a walk', '2024-11-05', 'TRIP', 1),
(10, 'Day 1', 'Went for a walk', '2024-11-20', 'ETC', 1),
(2, 'Day 2', 'Started new project', '2024-01-02', 'GROWTH', 2),
(3, 'Day 3', 'Workout session', '2024-01-03', 'EXERCISE', 3),
(4, 'Day 4', 'Visited new city', '2024-01-04', 'TRIP', 4),
(5, 'Day 5', 'Random thoughts', '2024-01-05', 'ETC', 5);

-- INSERT INTO 'schedule' table
INSERT INTO schedule (s_idx, s_title, s_content, s_start, s_end, s_location, s_color, cal_idx) VALUES
(1, 'Meeting', 'Team meeting', '2024-01-10 10:00:00', '2024-01-10 11:00:00', 'Conference Room', 'BLUE', 1),
(2, 'Workout', 'Morning workout', '2024-01-11 06:00:00', '2024-01-11 07:00:00', 'Gym', 'GREEN', 2),
(3, 'Lunch with client', 'Business lunch', '2024-01-12 12:00:00', '2024-01-12 13:00:00', 'Restaurant', 'YELLOW', 3),
(4, 'Coding Session', 'Project coding', '2024-01-13 14:00:00', '2024-01-13 16:00:00', 'Office', 'VIOLET', 4),
(5, 'Daily walk', 'Evening walk', '2024-01-14 18:00:00', '2024-01-14 19:00:00', 'Park', 'ORANGE', 5);
