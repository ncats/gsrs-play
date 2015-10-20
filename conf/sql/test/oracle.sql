SELECT 
(select 
(case when count(*) = 1 
THEN 'worked' 
else 'failed' 
end) from (select VALUE from v$nls_parameters where parameter in ('NLS_CHARACTERSET')) where value = 'AL32UTF8') result,

(select 
(case when count(*) = 1 
THEN 'character encoding properly set' 
else 'character encoding not properly set, international encoding may cause problems' 
end) from (select VALUE from v$nls_parameters where parameter in ('NLS_CHARACTERSET')) where value = 'AL32UTF8') message,

'ALTER DATABASE '||ora_database_name||' CHARACTER SET AL32UTF8' as sql

FROM dual v

