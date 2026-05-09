const fs = require('fs');
const path = require('path');

const input = process.argv[2];
const output = process.argv[3];

if (!input || !output) {
  console.error('Usage: node mysql_to_postgres_dump.js <mysql_dump.sql> <postgres_dump.sql>');
  process.exit(1);
}

let sql = fs.readFileSync(path.resolve(input), 'utf8');

sql = sql.replace(/\r\n/g, '\n');
sql = sql.replace(/\/\*[\s\S]*?\*\//g, '');
sql = sql.replace(/SET FOREIGN_KEY_CHECKS\s*=\s*0\s*;?/gi, '');
sql = sql.replace(/ENGINE=InnoDB/gi, '');
sql = sql.replace(/DEFAULT CHARSET=[^\s;]+/gi, '');
sql = sql.replace(/COLLATE=[^\s,;]+/gi, '');
sql = sql.replace(/AUTO_INCREMENT=\d+/gi, '');
sql = sql.replace(/AUTO_INCREMENT/gi, '');
sql = sql.replace(/unsigned/gi, '');
sql = sql.replace(/`/g, '"');
sql = sql.replace(/\bint\(\d+\)/gi, 'INTEGER');
sql = sql.replace(/\bbigint\(\d+\)/gi, 'BIGINT');
sql = sql.replace(/\btinyint\(\d+\)/gi, 'SMALLINT');
sql = sql.replace(/\bdatetime\b/gi, 'TIMESTAMP');
sql = sql.replace(/\blongtext\b/gi, 'TEXT');
sql = sql.replace(/\bmediumtext\b/gi, 'TEXT');
sql = sql.replace(/\btext\s+COMMENT\s+'[^']*'/gi, 'TEXT');
sql = sql.replace(/\bdouble\b/gi, 'DOUBLE PRECISION');
sql = sql.replace(/\bfloat\b/gi, 'DOUBLE PRECISION');
sql = sql.replace(/\bjson\b/gi, 'JSONB');
sql = sql.replace(/DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP/gi, 'DEFAULT CURRENT_TIMESTAMP');
sql = sql.replace(/ON UPDATE CURRENT_TIMESTAMP/gi, '');
sql = sql.replace(/\)\s*USING BTREE/gi, ')');
sql = sql.replace(/,\s*KEY\s+"([^"]+)"\s*\(([^)]+)\)/gi, '');
sql = sql.replace(/,\s*UNIQUE KEY\s+"([^"]+)"\s*\(([^)]+)\)/gi, '');
sql = sql.replace(/,\s*CONSTRAINT\s+"([^"]+)"\s*FOREIGN KEY[\s\S]*?(?=,\s*("|PRIMARY|KEY|UNIQUE)|\)\s*;)/gi, '');
sql = sql.replace(/,\s*\n\s*\)/g, '\n)');

sql = sql.replace(/CREATE TABLE\s+"([^"]+)"\s*\(([\s\S]*?)\);/gi, (all, table, body) => {
  const lines = body.split('\n').map((line) => line.trim()).filter(Boolean);
  const next = lines.map((line) => {
    if (/^"[^"]+"\s+BIGINT\s+NOT NULL\s*,?$/i.test(line) && /PRIMARY KEY/i.test(body) === false) {
      return line;
    }
    if (/^"[^"]+"\s+BIGINT\s+NOT NULL/i.test(line) && /AUTO_INCREMENT/i.test(all)) {
      return line.replace(/\s+BIGINT\s+NOT NULL/i, ' BIGSERIAL');
    }
    if (/^"[^"]+"\s+INTEGER\s+NOT NULL/i.test(line) && /AUTO_INCREMENT/i.test(all)) {
      return line.replace(/\s+INTEGER\s+NOT NULL/i, ' SERIAL');
    }
    return line;
  });
  return `CREATE TABLE "${table}" (\n  ${next.join('\n  ')}\n);\n`;
});

sql = sql.replace(/,\s*\n\s*PRIMARY KEY/gi, ',\n  PRIMARY KEY');
sql = sql.replace(/\n{3,}/g, '\n\n');

const header = [
  '-- Generated from MySQL dump for PostgreSQL import',
  'SET client_encoding = \'UTF8\';',
  'SET standard_conforming_strings = on;',
  ''
].join('\n');

fs.writeFileSync(path.resolve(output), header + sql, 'utf8');
console.log(`written: ${output}`);
