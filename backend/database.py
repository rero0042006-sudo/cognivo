"""Neon PostgreSQL Database Connection Pool & Query Module for Cogniva Backend"""

import os
import logging
from typing import Optional, List, Dict, Any
from dotenv import load_dotenv
import psycopg
from psycopg.rows import dict_row
from psycopg_pool import ConnectionPool
from anyio import to_thread

load_dotenv()

logger = logging.getLogger("cogniva_db")

_pool: Optional[ConnectionPool] = None

def get_database_url() -> str:
    """Return sanitized PostgreSQL database connection URL."""
    url = os.getenv("DATABASE_URL", "").strip()
    if not url:
        return ""
    if "channel_binding=" in url:
        parts = url.split("&")
        url = "&".join(p for p in parts if not p.startswith("channel_binding="))
    return url

def is_db_configured() -> bool:
    url = get_database_url()
    return bool(url and url.startswith("postgres"))

def get_connection_pool() -> ConnectionPool:
    global _pool
    if _pool is None or _pool.closed:
        url = get_database_url()
        if not url:
            raise ValueError("DATABASE_URL is not set.")
        _pool = ConnectionPool(
            conninfo=url,
            min_size=1,
            max_size=10,
            timeout=30.0,
            max_idle=300.0,
            kwargs={"row_factory": dict_row}
        )
        _pool.open()
    return _pool

def _sync_execute_query(query: str, params: tuple) -> List[Dict[str, Any]]:
    pool = get_connection_pool()
    with pool.connection() as conn:
        with conn.cursor() as cur:
            cur.execute(query, params)
            results = cur.fetchall()
            return [dict(r) for r in results]

def _sync_execute_statement(statement: str, params: tuple) -> int:
    pool = get_connection_pool()
    with pool.connection() as conn:
        with conn.cursor() as cur:
            cur.execute(statement, params)
            conn.commit()
            return cur.rowcount

async def execute_query(query: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
    """Execute a SELECT query asynchronously using the pooled worker."""
    return await to_thread.run_sync(_sync_execute_query, query, params or ())

async def execute_statement(statement: str, params: Optional[tuple] = None) -> int:
    """Execute an INSERT/UPDATE/DELETE statement asynchronously with connection pooling."""
    return await to_thread.run_sync(_sync_execute_statement, statement, params or ())
