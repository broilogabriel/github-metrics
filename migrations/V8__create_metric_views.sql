create view projects_metrics as
  select r."id"                                                              "id"
       , r."name"                                                            "name"
       , count(distinct pr.user_id)                                          total_contributors
       , count(*) filter (where merged_at is not null)                       total_commits
       , count(*) filter (where closed_at is null and merged_at is null)     total_open_pull_requests
       , count(*) filter (where closed_at is not null and merged_at is null) total_closed_pull_requests
    from pull_requests pr
    join repositories  r on r.id = pr.repository_id
group by r."id"
       , r."name"
;

create view contributors_metrics as
  select u."id"                                                              "id"
       , u."login"                                                           "login"
       , count(distinct pr.repository_id)                                    total_projects
       , count(*) filter (where merged_at is not null)                       total_commits
       , count(*) filter (where closed_at is null and merged_at is null)     total_open_pull_requests
       , count(*) filter (where closed_at is not null and merged_at is null) total_closed_pull_requests
    from pull_requests pr
    join users         u on u.id = pr.user_id
group by u."id"
       , u."login";