create table if not exists post(
	id serial primary key,
	name varchar(255),
	text text,
	link text unique,
	created timestamp
);
