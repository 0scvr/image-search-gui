CREATE TABLE images (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	filename TEXT NOT NULL,
	histogram TEXT,
	hsvHistogram TEXT,
	grayscale INTEGER
);