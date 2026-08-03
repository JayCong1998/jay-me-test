# Development Demo Data

This folder contains local-only seed scripts for filling the application with realistic demo activity.

These scripts are not release migrations. Run them only against a development database.

## Scripts

- `seed_demo_activity.sql`: inserts demo users, records for CLASSIC / ALBUM / ABYSS modes, and album progress rows.

## Usage

```bash
mysql -uroot -proot jaymetest < database/dev/seed_demo_activity.sql
```

On Windows PowerShell, use `cmd /c` so MySQL reads the UTF-8 SQL file directly:

```powershell
cmd /c "mysql --default-character-set=utf8mb4 -uroot -proot jaymetest < database\dev\seed_demo_activity.sql"
```

The script is safe to rerun. It uses `demoNN@jaymetest.local` users and stable `round_id` values, so existing demo rows are skipped instead of duplicated. It does not delete or reset your manually generated data.
