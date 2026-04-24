# SCM Web – Changes Implemented

## Fixed bugs
- Fixed `/student/grades` crashing with a 500 / Whitelabel page.
- Added duplicate enrollment prevention.
- Fixed seat count synchronization so approved enrollments reduce availability for all users.
- Added the missing attendance feature.

## Major functional improvements
- Replaced fragile external JDBC/MySQL dependency with a local file-backed persistence layer for reliable local runs.
- Added student attendance, materials, payments, and course details pages.
- Added faculty attendance management and material upload workflows.
- Added department-head grade approval/rejection and department report download.
- Added admin course updates, faculty assignment, user activation/deactivation, and admin report download.
- Added payment success/failure handling, refund-on-drop logic, and persistence across restarts.
- Added no-cache filter and custom error page to improve session/logout behavior and avoid Whitelabel fallback.

## Persistence behavior
- The application now stores data in a local `scm_data/` folder created when the app runs.
- This keeps changes (registrations, approvals, grades, attendance, payments, materials metadata, user active state) across restarts on the same machine.
