/*
 * $Id$
 *
 * HyperSQL ODBC Driver
 *
 * Module:          connection.c
 *
 * Description:     This module contains routines related to
 *                  connecting to and disconnecting from the Postgres DBMS.
 *
 * Classes:         ConnectionClass (Functions prefix: "CC_")
 *
 * API functions:   SQLAllocConnect, SQLConnect, SQLDisconnect, SQLFreeConnect,
 *                  SQLBrowseConnect(NI)
 *
 * Comments:        See "notice.txt" for copyright and license details.
 *                  Significant modifications Copyright 2009 by
 *                  the HSQL Development Group.  Changes made by the HSQL
 *                  Development are documented precisely in the public HyperSQL
 *                  source code repository, available through http://hsqldb.org.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the
 *  Free Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA  02110-1301  USA
 *
 *-------
 *
 * Multibyte support    Eiji Tokuya 2001-03-15
 */

#include "connection.h"

#include <stdio.h>
#include <string.h>
#include <ctype.h>
#ifdef  WIN32
#else
#include <errno.h>
#endif /* WIN32 */

#include "environ.h"
#include "socket.h"
#include "statement.h"
#include "qresult.h"
#include "lobj.h"
#include "dlg_specific.h"
#include "loadlib.h"

#include "multibyte.h"

#include "pgapifunc.h"
#include "md5.h"

#define STMT_INCREMENT 16       /* how many statement holders to allocate
                                 * at a time */

#define PRN_NULLCHECK

static void CC_lookup_pg_version(ConnectionClass *self);
static void CC_lookup_lo(ConnectionClass *self);
static char *CC_create_errormsg(ConnectionClass *self);

extern GLOBAL_VALUES globals;


RETCODE     SQL_API
PGAPI_AllocConnect(
                   HENV henv,
                   HDBC FAR * phdbc)
{
    EnvironmentClass *env = (EnvironmentClass *) henv;
    ConnectionClass *conn;
    CSTR func = "PGAPI_AllocConnect";

    mylog("%s: entering...\n", func);

    conn = CC_Constructor();
    mylog("**** %s: henv = %p, conn = %p\n", func, henv, conn);

    if (!conn)
    {
        env->errormsg = "Couldn't allocate memory for Connection object.";
        env->errornumber = ENV_ALLOC_ERROR;
        *phdbc = SQL_NULL_HDBC;
        EN_log_error(func, "", env);
        return SQL_ERROR;
    }

    if (!EN_add_connection(env, conn))
    {
        env->errormsg = "Maximum number of connections exceeded.";
        env->errornumber = ENV_ALLOC_ERROR;
        CC_Destructor(conn);
        *phdbc = SQL_NULL_HDBC;
        EN_log_error(func, "", env);
        return SQL_ERROR;
    }

    if (phdbc)
        *phdbc = (HDBC) conn;

    return SQL_SUCCESS;
}


RETCODE     SQL_API
PGAPI_Connect(
              HDBC hdbc,
              const SQLCHAR FAR * szDSN,
              SQLSMALLINT cbDSN,
              const SQLCHAR FAR * szUID,
              SQLSMALLINT cbUID,
              const SQLCHAR FAR * szAuthStr,
              SQLSMALLINT cbAuthStr)
{
    ConnectionClass *conn = (ConnectionClass *) hdbc;
    ConnInfo   *ci;
    CSTR func = "PGAPI_Connect";
    RETCODE ret = SQL_SUCCESS;
    char    fchar;

    mylog("%s: entering..cbDSN=%hi.\n", func, cbDSN);

    if (!conn)
    {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    ci = &conn->connInfo;

    make_string(szDSN, cbDSN, ci->dsn, sizeof(ci->dsn));

    /* get the values for the DSN from the registry */
    memcpy(&ci->drivers, &globals, sizeof(globals));
    getDSNinfo(ci, CONN_OVERWRITE);
    logs_on_off(1, ci->drivers.debug, ci->drivers.commlog);
    /* initialize pg_version from connInfo.protocol    */
    CC_initialize_pg_version(conn);

    /*
     * override values from DSN info with UID and authStr(pwd) This only
     * occurs if the values are actually there.
     */
    fchar = ci->username[0]; /* save the first byte */ 
    make_string(szUID, cbUID, ci->username, sizeof(ci->username));
    if ('\0' == ci->username[0]) /* an empty string is specified */
        ci->username[0] = fchar; /* restore the original username */
    fchar = ci->password[0]; 
    make_string(szAuthStr, cbAuthStr, ci->password, sizeof(ci->password));
    if ('\0' == ci->password[0]) /* an empty string is specified */
        ci->password[0] = fchar; /* restore the original password */

    /* fill in any defaults */
    getDSNdefaults(ci);

    qlog("conn = %p, %s(DSN='%s', UID='%s', PWD='%s')\n", conn, func, ci->dsn, ci->username, ci->password ? "xxxxx" : "");

    if (CC_connect(conn, AUTH_REQ_OK, NULL) <= 0)
    {
        /* Error messages are filled in */
        CC_log_error(func, "Error on CC_connect", conn);
        ret = SQL_ERROR;
    }

    mylog("%s: returning..%d.\n", func, ret);

    return ret;
}


RETCODE     SQL_API
PGAPI_BrowseConnect(
                HDBC hdbc,
                const SQLCHAR FAR * szConnStrIn,
                SQLSMALLINT cbConnStrIn,
                SQLCHAR FAR * szConnStrOut,
                SQLSMALLINT cbConnStrOutMax,
                SQLSMALLINT FAR * pcbConnStrOut)
{
    CSTR func = "PGAPI_BrowseConnect";
    ConnectionClass *conn = (ConnectionClass *) hdbc;

    mylog("%s: entering...\n", func);

    CC_set_error(conn, CONN_NOT_IMPLEMENTED_ERROR, "Function not implemented", func);
    return SQL_ERROR;
}


/* Drop any hstmts open on hdbc and disconnect from database */
RETCODE     SQL_API
PGAPI_Disconnect(
                 HDBC hdbc)
{
    ConnectionClass *conn = (ConnectionClass *) hdbc;
    CSTR func = "PGAPI_Disconnect";


    mylog("%s: entering...\n", func);

    if (!conn)
    {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    qlog("conn=%p, %s\n", conn, func);

    if (conn->status == CONN_EXECUTING)
    {
        CC_set_error(conn, CONN_IN_USE, "A transaction is currently being executed", func);
        return SQL_ERROR;
    }

    logs_on_off(-1, conn->connInfo.drivers.debug, conn->connInfo.drivers.commlog);
    mylog("%s: about to CC_cleanup\n", func);

    /* Close the connection and free statements */
    CC_cleanup(conn);

    mylog("%s: done CC_cleanup\n", func);
    mylog("%s: returning...\n", func);

    return SQL_SUCCESS;
}


RETCODE     SQL_API
PGAPI_FreeConnect(
                  HDBC hdbc)
{
    ConnectionClass *conn = (ConnectionClass *) hdbc;
    CSTR func = "PGAPI_FreeConnect";
    EnvironmentClass *env;

    mylog("%s: entering...\n", func);
    mylog("**** in %s: hdbc=%p\n", func, hdbc);

    if (!conn)
    {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    /* Remove the connection from the environment */
    if (NULL != (env = CC_get_env(conn)) &&
        !EN_remove_connection(env, conn))
    {
        CC_set_error(conn, CONN_IN_USE, "A transaction is currently being executed", func);
        return SQL_ERROR;
    }

    CC_Destructor(conn);

    mylog("%s: returning...\n", func);

    return SQL_SUCCESS;
}

void
CC_conninfo_init(ConnInfo *conninfo)
{
        memset(conninfo, 0, sizeof(ConnInfo));
        conninfo->disallow_premature = -1;
        conninfo->allow_keyset = -1;
        conninfo->lf_conversion = -1;
        conninfo->true_is_minus1 = -1;
        conninfo->int8_as = -101;
        conninfo->bytea_as_longvarbinary = -1;
        conninfo->use_server_side_prepare = -1;
        conninfo->lower_case_identifier = -1;
        conninfo->rollback_on_error = -1;
        conninfo->force_abbrev_connstr = -1;
        conninfo->bde_environment = -1;
        conninfo->fake_mss = -1;
        conninfo->cvt_null_date_string = -1;
        conninfo->autocommit_public = SQL_AUTOCOMMIT_ON;
        conninfo->accessible_only = -1;
#ifdef  _HANDLE_ENLIST_IN_DTC_
        conninfo->xa_opt = -1;
#endif /* _HANDLE_ENLIST_IN_DTC_ */
        memcpy(&(conninfo->drivers), &globals, sizeof(globals));
}

#ifdef  WIN32
extern  int platformId;
#endif /* WIN32 */

/*
 *      IMPLEMENTATION CONNECTION CLASS
 */

static void
reset_current_schema(ConnectionClass *self)
{
    if (self->current_schema)
    {
        free(self->current_schema);
        self->current_schema = NULL;
    }
}

ConnectionClass *
CC_Constructor()
{
    ConnectionClass *rv, *retrv = NULL;

    rv = (ConnectionClass *) calloc(sizeof(ConnectionClass), 1);

    if (rv != NULL)
    {
        rv->status = CONN_NOT_CONNECTED;
        rv->transact_status = CONN_IN_AUTOCOMMIT;       /* autocommit by default */

        CC_conninfo_init(&(rv->connInfo));
        rv->sock = SOCK_Constructor(rv);
        if (!rv->sock)
            goto cleanup;

        rv->stmts = (StatementClass **) malloc(sizeof(StatementClass *) * STMT_INCREMENT);
        if (!rv->stmts)
            goto cleanup;
        memset(rv->stmts, 0, sizeof(StatementClass *) * STMT_INCREMENT);

        rv->num_stmts = STMT_INCREMENT;
#if (ODBCVER >= 0x0300)
        rv->descs = (DescriptorClass **) malloc(sizeof(DescriptorClass *) * STMT_INCREMENT);
        if (!rv->descs)
            goto cleanup;
        memset(rv->descs, 0, sizeof(DescriptorClass *) * STMT_INCREMENT);

        rv->num_descs = STMT_INCREMENT;
#endif /* ODBCVER */

        rv->lobj_type = PG_TYPE_LO_UNDEFINED;
        rv->driver_version = ODBCVER;
#ifdef  WIN32
        if (VER_PLATFORM_WIN32_WINDOWS == platformId && rv->driver_version > 0x0300)
            rv->driver_version = 0x0300;
#endif /* WIN32 */
        if (isMsAccess())
            rv->ms_jet = 1;
        rv->isolation = SQL_TXN_READ_COMMITTED;
        rv->mb_maxbyte_per_char = 1;
        rv->max_identifier_length = -1;
        rv->escape_in_literal = ESCAPE_IN_LITERAL;

        /* Initialize statement options to defaults */
        /* Statements under this conn will inherit these options */

        InitializeStatementOptions(&rv->stmtOptions);
        InitializeARDFields(&rv->ardOptions);
        InitializeAPDFields(&rv->apdOptions);
#ifdef  _HANDLE_ENLIST_IN_DTC_
        // rv->asdum = NULL;
#endif /* _HANDLE_ENLIST_IN_DTC_ */
        INIT_CONNLOCK(rv);
        INIT_CONN_CS(rv);
        retrv = rv;
    }

cleanup:
    if (rv && !retrv)
        CC_Destructor(rv);
    return retrv;
}


char
CC_Destructor(ConnectionClass *self)
{
    mylog("enter CC_Destructor, self=%p\n", self);

    if (self->status == CONN_EXECUTING)
        return 0;

    CC_cleanup(self);           /* cleanup socket and statements */

    mylog("after CC_Cleanup\n");

    /* Free up statement holders */
    if (self->stmts)
    {
        free(self->stmts);
        self->stmts = NULL;
    }
#if (ODBCVER >= 0x0300)
    if (self->descs)
    {
        free(self->descs);
        self->descs = NULL;
    }
#endif /* ODBCVER */
    mylog("after free statement holders\n");

    NULL_THE_NAME(self->schemaIns);
    NULL_THE_NAME(self->tableIns);
    if (self->__error_message)
        free(self->__error_message);
    DELETE_CONN_CS(self);
    DELETE_CONNLOCK(self);
    free(self);

    mylog("exit CC_Destructor\n");

    return 1;
}


/*  Return how many cursors are opened on this connection */
int
CC_cursor_count(ConnectionClass *self)
{
    StatementClass *stmt;
    int         i,
                count = 0;
    QResultClass        *res;

    mylog("CC_cursor_count: self=%p, num_stmts=%d\n", self, self->num_stmts);

    CONNLOCK_ACQUIRE(self);
    for (i = 0; i < self->num_stmts; i++)
    {
        stmt = self->stmts[i];
        if (stmt && (res = SC_get_Result(stmt)) && QR_get_cursor(res))
            count++;
    }
    CONNLOCK_RELEASE(self);

    mylog("CC_cursor_count: returning %d\n", count);

    return count;
}


void
CC_clear_error(ConnectionClass *self)
{
    if (!self)  return;
    CONNLOCK_ACQUIRE(self);
    self->__error_number = 0;
    if (self->__error_message)
    {
        free(self->__error_message);
        self->__error_message = NULL;
    }
    self->sqlstate[0] = '\0';
    self->errormsg_created = FALSE;
    CONNLOCK_RELEASE(self);
}


CSTR    bgncmd = "BEGIN";
CSTR    rbkcmd = "ROLLBACK";
CSTR    semi_colon = ";";
/*
 *  Used to begin a transaction.
 */
char
CC_begin(ConnectionClass *self)
{
    char    ret = TRUE;
    if (!CC_is_in_trans(self))
    {
        QResultClass *res = CC_send_query(self, bgncmd, NULL, 0, NULL);
        mylog("CC_begin:  sending BEGIN!\n");

        ret = QR_command_maybe_successful(res);
        QR_Destructor(res);
    }

    return ret;
}

/*
 *  Used to commit a transaction.
 *  We are almost always in the middle of a transaction.
 */
char
CC_commit(ConnectionClass *self)
{
    char    ret = TRUE;
    if (CC_is_in_trans(self))
    {
        QResultClass *res = CC_send_query(self, "COMMIT", NULL, 0, NULL);
        mylog("CC_commit:  sending COMMIT!\n");
        ret = QR_command_maybe_successful(res);
        QR_Destructor(res);
    }

    return ret;
}

/*
 *  Used to cancel a transaction.
 *  We are almost always in the middle of a transaction.
 */
char
CC_abort(ConnectionClass *self)
{
    char    ret = TRUE;
    if (CC_is_in_trans(self))
    {
        QResultClass *res = CC_send_query(self, rbkcmd, NULL, 0, NULL);
        mylog("CC_abort:  sending ABORT!\n");
        ret = QR_command_maybe_successful(res);
        QR_Destructor(res);
    }

    return ret;
}

/* This is called by SQLSetConnectOption etc also */
char
CC_set_autocommit(ConnectionClass *self, BOOL on)
{
    CSTR func = "CC_set_autocommit";
    BOOL currsts = CC_is_in_autocommit(self);

    if ((on && currsts) ||
        (!on && !currsts))
        return on;
    mylog("%s: %d->%d\n", func, currsts, on);
    if (CC_is_in_trans(self))
        CC_commit(self);
    if (on)
        self->transact_status |= CONN_IN_AUTOCOMMIT;
    else
        self->transact_status &= ~CONN_IN_AUTOCOMMIT;

    return on;
}

/* This is called by SQLDisconnect also */
char
CC_cleanup(ConnectionClass *self)
{
    int         i;
    StatementClass *stmt;
    DescriptorClass *desc;

    if (self->status == CONN_EXECUTING)
        return FALSE;

    mylog("in CC_Cleanup, self=%p\n", self);

    /* Cancel an ongoing transaction */
    /* We are always in the middle of a transaction, */
    /* even if we are in auto commit. */
    if (self->sock)
    {
        CC_abort(self);

        mylog("after CC_abort\n");

        /* This actually closes the connection to the dbase */
        SOCK_Destructor(self->sock);
        self->sock = NULL;
    }

    mylog("after SOCK destructor\n");

    /* Free all the stmts on this connection */
    for (i = 0; i < self->num_stmts; i++)
    {
        stmt = self->stmts[i];
        if (stmt)
        {
            stmt->hdbc = NULL;  /* prevent any more dbase interactions */

            SC_Destructor(stmt);

            self->stmts[i] = NULL;
        }
    }
#if (ODBCVER >= 0x0300)
    /* Free all the descs on this connection */
    for (i = 0; i < self->num_descs; i++)
    {
        desc = self->descs[i];
        if (desc)
        {
            DC_get_conn(desc) = NULL;   /* prevent any more dbase interactions */
            DC_Destructor(desc);
            free(desc);
            self->descs[i] = NULL;
        }
    }
#endif /* ODBCVER */

    /* Check for translation dll */
#ifdef WIN32
    if (self->translation_handle)
    {
        FreeLibrary(self->translation_handle);
        self->translation_handle = NULL;
    }
#endif

    self->status = CONN_NOT_CONNECTED;
    self->transact_status = CONN_IN_AUTOCOMMIT;
    CC_conninfo_init(&(self->connInfo));
    if (self->original_client_encoding)
    {
        free(self->original_client_encoding);
        self->original_client_encoding = NULL;
    }
    if (self->current_client_encoding)
    {
        free(self->current_client_encoding);
        self->current_client_encoding = NULL;
    }
    if (self->server_encoding)
    {
        free(self->server_encoding);
        self->server_encoding = NULL;
    }
    reset_current_schema(self);
    /* Free cached table info */
    if (self->col_info)
    {
        for (i = 0; i < self->ntables; i++)
        {
            if (self->col_info[i]->result)  /* Free the SQLColumns result structure */
                QR_Destructor(self->col_info[i]->result);

            NULL_THE_NAME(self->col_info[i]->schema_name);
            NULL_THE_NAME(self->col_info[i]->table_name);
            free(self->col_info[i]);
        }
        free(self->col_info);
        self->col_info = NULL;
    }
    self->ntables = 0;
    self->coli_allocated = 0;
    if (self->num_discardp > 0 && self->discardp)
    {
        for (i = 0; i < self->num_discardp; i++)
            free(self->discardp[i]);
        self->num_discardp = 0;
    }
    if (self->discardp)
    {
        free(self->discardp);
        self->discardp = NULL;
    }

    mylog("exit CC_Cleanup\n");
    return TRUE;
}


int
CC_set_translation(ConnectionClass *self)
{

#ifdef WIN32
    CSTR    func = "CC_set_translation";

    if (self->translation_handle != NULL)
    {
        FreeLibrary(self->translation_handle);
        self->translation_handle = NULL;
    }

    if (self->connInfo.translation_dll[0] == 0)
        return TRUE;

    self->translation_option = atoi(self->connInfo.translation_option);
    self->translation_handle = LoadLibrary(self->connInfo.translation_dll);

    if (self->translation_handle == NULL)
    {
        CC_set_error(self, CONN_UNABLE_TO_LOAD_DLL, "Could not load the translation DLL.", func);
        return FALSE;
    }

    self->DataSourceToDriver
        = (DataSourceToDriverProc) GetProcAddress(self->translation_handle,
                                                "SQLDataSourceToDriver");

    self->DriverToDataSource
        = (DriverToDataSourceProc) GetProcAddress(self->translation_handle,
                                                "SQLDriverToDataSource");

    if (self->DataSourceToDriver == NULL || self->DriverToDataSource == NULL)
    {
        CC_set_error(self, CONN_UNABLE_TO_LOAD_DLL, "Could not find translation DLL functions.", func);
        return FALSE;
    }
#endif
    return TRUE;
}

static  int
md5_auth_send(ConnectionClass *self, const char *salt)
{
    char    *pwd1 = NULL, *pwd2 = NULL;
    ConnInfo   *ci = &(self->connInfo);
    SocketClass *sock = self->sock;
    size_t      md5len;

inolog("md5 pwd=%s user=%s salt=%02x%02x%02x%02x%02x\n", ci->password, ci->username, (UCHAR)salt[0], (UCHAR)salt[1], (UCHAR)salt[2], (UCHAR)salt[3], (UCHAR)salt[4]);
    if (!(pwd1 = malloc(MD5_PASSWD_LEN + 1)))
        return 1;
    if (!EncryptMD5(ci->password, ci->username, strlen(ci->username), pwd1))
    {
        free(pwd1);
        return 1;
    } 
    if (!(pwd2 = malloc(MD5_PASSWD_LEN + 1)))
    {
        free(pwd1);
        return 1;
    } 
    if (!EncryptMD5(pwd1 + strlen("md5"), salt, 4, pwd2))
    {
        free(pwd2);
        free(pwd1);
        return 1;
    }
    free(pwd1);
inolog("putting p and %s\n", pwd2);
        SOCK_put_char(sock, 'p');
    md5len = strlen(pwd2);
    SOCK_put_int(sock, (Int4) (4 + md5len + 1), 4);
    SOCK_put_n_char(sock, pwd2, (Int4) (md5len + 1));
    SOCK_flush_output(sock);
inolog("sockerr=%d\n", SOCK_get_errcode(sock));
    free(pwd2);
    return 0; 
}

int
EatReadyForQuery(ConnectionClass *conn)
{
    int id = 0;

    BOOL    is_in_error_trans = CC_is_in_error_trans(conn);
    switch (id = SOCK_get_char(conn->sock))
    {
        case 'I':
            if (CC_is_in_trans(conn))
            {
                if (is_in_error_trans)
                    CC_on_abort(conn, NO_TRANS);
                else
                    CC_on_commit(conn);
            }
            break;
        case 'T':
            CC_set_in_trans(conn);
            CC_set_no_error_trans(conn);
            if (is_in_error_trans)
                CC_on_abort_partial(conn);
            break;
        case 'E':
            CC_set_in_error_trans(conn);
            break;  
    }
mylog("## Server report Trans STATUS (%d)\n", id);
    return id;  
}

int
handle_error_message(ConnectionClass *self, char *msgbuf, size_t buflen, char *sqlstate, const char *comment, QResultClass *res)
{
    BOOL    msg_truncated = FALSE, truncated, hasmsg = FALSE;
    SocketClass *sock = self->sock;
    ConnInfo    *ci = &(self->connInfo);
    char    msgbuffer[ERROR_MSG_LENGTH];
    UDWORD  abort_opt;
    size_t  msgl;

    inolog("handle_error_message protocol=%s\n", ci->protocol);
    truncated = SOCK_get_string(sock, msgbuffer,sizeof(msgbuffer));

    msgbuf[0] = '\0';
    for (;msgbuffer[0];)
    {
        mylog("%s: 'E' - %s\n", comment, msgbuffer);
        qlog("ERROR from backend during %s: '%s'\n", comment, msgbuffer);
        msgl = strlen(msgbuffer + 1);
        switch (msgbuffer[0])
        {
            case 'S':
                if (buflen > 0)
                {
                    strncat(msgbuf, msgbuffer + 1, buflen);
                    buflen -= msgl;
                }
                if (buflen > 0)
                {
                    strncat(msgbuf, ": ", buflen);
                    buflen -= 2;
                }
                break;
            case 'M':
            case 'D':
                if (buflen > 0)
                {
                    if (hasmsg)
                    {
                        strcat(msgbuf, "\n");
                        buflen--;
                    }
                    if (buflen > 0)
                    {
                        strncat(msgbuf, msgbuffer + 1, buflen);
                        buflen -= msgl;
                    }
                }
                if (truncated)
                    msg_truncated = truncated;
                hasmsg = TRUE;
                break;
            case 'C':
                if (sqlstate)
                    strncpy_null(sqlstate, msgbuffer + 1, 8);
                break;
        }
        if (buflen < 0)
            buflen = 0;
        while (truncated)
            truncated = SOCK_get_string(sock, msgbuffer, sizeof(msgbuffer));
        truncated = SOCK_get_string(sock, msgbuffer, sizeof(msgbuffer));
    }
    abort_opt = 0;
    if (!strncmp(msgbuf, "FATAL", 5))
    {
        CC_set_errornumber(self, CONNECTION_SERVER_REPORTED_ERROR);
        abort_opt = CONN_DEAD;
    }
    else
    {
        CC_set_errornumber(self, CONNECTION_SERVER_REPORTED_WARNING);
        if (CC_is_in_trans(self))
            CC_set_in_error_trans(self);
    }
    if (0 != abort_opt
#ifdef  _LEGACY_MODE_
        || TRUE
#endif /* _LEGACY_NODE_ */
       )
        CC_on_abort(self, abort_opt);
    if (res)
    {
        QR_set_rstatus(res, PORES_FATAL_ERROR);
        QR_set_message(res, msgbuf);
        QR_set_aborted(res, TRUE);
    }

    return msg_truncated;
}

int
handle_notice_message(ConnectionClass *self, char *msgbuf, size_t buflen, char *sqlstate, const char *comment, QResultClass *res)
{
    BOOL    msg_truncated = FALSE, truncated, hasmsg = FALSE;
    SocketClass *sock = self->sock;
    char    msgbuffer[ERROR_MSG_LENGTH];

    size_t  msgl;

    msgbuf[0] = '\0';
    for (;;)
    {
        truncated = SOCK_get_string(sock, msgbuffer, sizeof(msgbuffer));
        if (!msgbuffer[0])
            break;

        mylog("%s: 'N' - %s\n", comment, msgbuffer);
        qlog("NOTICE from backend during %s: '%s'\n", comment, msgbuffer);
        msgl = strlen(msgbuffer + 1);
        switch (msgbuffer[0])
        {
            case 'S':
                if (buflen > 0)
                {
                    strncat(msgbuf, msgbuffer + 1, buflen);
                    buflen -= msgl;
                }
                if (buflen > 0)
                {
                    strncat(msgbuf, ": ", buflen);
                    buflen -= 2;
                }
                break;
            case 'M':
            case 'D':
                if (buflen > 0)
                {
                    if (hasmsg)
                    {
                        strcat(msgbuf, "\n");
                        buflen--;
                    }
                    if (buflen > 0)
                    {
                        strncat(msgbuf, msgbuffer + 1, buflen);
                        buflen -= msgl;
                    }
                }
                else
                    msg_truncated = TRUE;
                if (truncated)
                    msg_truncated = truncated;
                hasmsg = TRUE;
                break;
            case 'C':
                if (sqlstate && !sqlstate[0] && strcmp(msgbuffer + 1, "00000"))
                    strncpy_null(sqlstate, msgbuffer + 1, 8);
                break;
        }
        if (buflen < 0)
            msg_truncated = TRUE;
        while (truncated)
            truncated = SOCK_get_string(sock, msgbuffer, sizeof(msgbuffer));
    }
if (res)
{
    if (QR_command_successful(res))
        QR_set_rstatus(res, PORES_NONFATAL_ERROR);
    QR_set_notice(res, msgbuf);  /* will dup this string */
}

return msg_truncated;
}

CSTR std_cnf_strs = "standard_conforming_strings";

void    getParameterValues(ConnectionClass *conn)
{
SocketClass *sock = conn->sock;
/* ERROR_MSG_LENGTH is suffcient */
char nambuffer[ERROR_MSG_LENGTH + 1];
char valbuffer[ERROR_MSG_LENGTH + 1];

SOCK_get_string(sock, nambuffer, sizeof(nambuffer));
inolog("parameter name=%s\n", nambuffer);
    SOCK_get_string(sock, valbuffer, sizeof(valbuffer));
    if (stricmp(nambuffer, "server_encoding") == 0)
    {
        if (conn->server_encoding)
            free(conn->server_encoding);
        conn->server_encoding = strdup(valbuffer);
    }
    else if (stricmp(nambuffer, "client_encoding") == 0)
    {
        if (conn->current_client_encoding)
            free(conn->current_client_encoding);
        conn->current_client_encoding = strdup(valbuffer);
    }
    else if (stricmp(nambuffer, std_cnf_strs) == 0)
    {
        mylog("%s=%s\n", std_cnf_strs, valbuffer); 
        if (stricmp(valbuffer, "on") == 0)
        {
            conn->escape_in_literal = '\0';
        }
    }
    else if (stricmp(nambuffer, "server_version") == 0)
    {
        char    szVersion[32];
        int major, minor;

        strncpy_null(conn->pg_version, valbuffer, sizeof(conn->pg_version));
        strcpy(szVersion, "0.0");
        if (sscanf(conn->pg_version, "%d.%d", &major, &minor) >= 2)
        {
            snprintf(szVersion, sizeof(szVersion), "%d.%d", major, minor);
            conn->pg_version_major = major;
            conn->pg_version_minor = minor;
        }
        conn->pg_version_number = (float) atof(szVersion);
        if (PG_VERSION_GE(conn, 7.3))
            conn->schema_support = 1;

        mylog("Got the PostgreSQL version string: '%s'\n", conn->pg_version);
        mylog("Extracted PostgreSQL version number: '%1.1f'\n", conn->pg_version_number);
        qlog("    [ PostgreSQL version string = '%s' ]\n", conn->pg_version);
        qlog("    [ PostgreSQL version number = '%1.1f' ]\n", conn->pg_version_number);
    }

inolog("parameter value=%s\n", valbuffer);
}

static int  protocol3_opts_array(ConnectionClass *self, const char *opts[][2], BOOL libpqopt, int dim_opts)
{
    ConnInfo    *ci = &(self->connInfo);
    const   char    *enc = NULL;
    int cnt;

    cnt = 0;
    if (libpqopt && ci->server[0])
    {
        opts[cnt][0] = "host";      opts[cnt++][1] = ci->server;
    }
    if (libpqopt && ci->port[0])
    {
        opts[cnt][0] = "port";      opts[cnt++][1] = ci->port;
    }
    if (ci->database[0])
    {
        if (libpqopt)
        {
            opts[cnt][0] = "dbname";    opts[cnt++][1] = ci->database;
        }
        else
        {
            opts[cnt][0] = "database";  opts[cnt++][1] = ci->database;
        }
    }
    if (ci->username[0])
    {
        opts[cnt][0] = "user";      opts[cnt++][1] = ci->username;
    }
    if (libpqopt)
    {
        if (ci->sslmode[0])
        {
            opts[cnt][0] = "sslmode";   opts[cnt++][1] = ci->sslmode;
        }
        if (ci->password[0])
        {
            opts[cnt][0] = "password";  opts[cnt++][1] = ci->password;
        }
    }
    else
    {
        /* DateStyle */
        opts[cnt][0] = "DateStyle"; opts[cnt++][1] = "ISO";
        /* extra_float_digits */
        opts[cnt][0] = "extra_float_digits";    opts[cnt++][1] = "2";
        /* geqo */
        opts[cnt][0] = "geqo";
        if (ci->drivers.disable_optimizer)
            opts[cnt++][1] = "off";
        else
            opts[cnt++][1] = "on";
        /* client_encoding */
        enc = get_environment_encoding(self, self->original_client_encoding, NULL, TRUE);
        if (enc)
        {
            mylog("startup client_encoding=%s\n", enc);
            opts[cnt][0] = "client_encoding"; opts[cnt++][1] = enc;
        }
    }

    return cnt;
}

/*
 * This function has a misleading name.  It does a transmission in addition
 * to building the packet.
 */
static int  protocol3_packet_build(ConnectionClass *self)
{
    CSTR    func = "protocol3_packet_build";
    SocketClass *sock = self->sock;
    size_t  slen;
    char    *packet, *ppacket;
    ProtocolVersion pversion;
    const   char    *opts[20][2];
    int cnt, i;

    cnt = protocol3_opts_array(self, opts, FALSE, sizeof(opts) / sizeof(opts[0]));

    slen =  sizeof(ProtocolVersion);
    for (i = 0; i < cnt; i++)
    {
        slen += (strlen(opts[i][0]) + 1);
        slen += (strlen(opts[i][1]) + 1);
    }
    slen++;
                
    if (packet = malloc(slen), !packet)
    {
        CC_set_error(self, CONNECTION_SERVER_NOT_REACHED, "Could not allocate a startup packet", func);
        return 0;
    }

    mylog("sizeof startup packet = %d\n", slen);

    sock->pversion = PG_PROTOCOL_LATEST;
    /* Send length of Authentication Block */
    SOCK_put_int(sock, (Int4) (slen + 4), 4);

    ppacket = packet;
    pversion = (ProtocolVersion) htonl(sock->pversion);
    memcpy(ppacket, &pversion, sizeof(pversion));
    ppacket += sizeof(pversion);
    for (i = 0; i < cnt; i++)
    { 
        strcpy(ppacket, opts[i][0]);
        ppacket += (strlen(opts[i][0]) + 1);
        strcpy(ppacket, opts[i][1]);
        ppacket += (strlen(opts[i][1]) + 1);
    }
    *ppacket = '\0';

    SOCK_put_n_char(sock, packet, (Int4) slen);
    SOCK_flush_output(sock);
    free(packet);

    return 1;
}

CSTR    l_login_timeout = "connect_timeout";
static char *protocol3_opts_build(ConnectionClass *self)
{
    CSTR    func = "protocol3_opts_build";
    size_t  slen;
    char    *conninfo, *ppacket;
    const   char    *opts[20][2];
    int cnt, i;
    BOOL    blankExist;

    cnt = protocol3_opts_array(self, opts, TRUE, sizeof(opts) / sizeof(opts[0]));

    slen =  sizeof(ProtocolVersion);
    for (i = 0, slen = 0; i < cnt; i++)
    {
        slen += (strlen(opts[i][0]) + 2 + 2); /* add 2 bytes for safety (literal quotes) */
        slen += strlen(opts[i][1]);
    }
    if (self->login_timeout > 0)
    {
        char    tmout[16];

        slen += (strlen(l_login_timeout) + 2 + 2);
        snprintf(tmout, sizeof(tmout), FORMAT_UINTEGER, self->login_timeout);
        slen += strlen(tmout);
    }
    slen++;
                
    if (conninfo = malloc(slen), !conninfo)
    {
        CC_set_error(self, CONNECTION_SERVER_NOT_REACHED, "Could not allocate a connectdb option", func);
        return 0;
    }

    mylog("sizeof connectdb option = %d\n", slen);

    for (i = 0, ppacket = conninfo; i < cnt; i++)
    { 
        sprintf(ppacket, " %s=", opts[i][0]);
        ppacket += (strlen(opts[i][0]) + 2);
        blankExist = FALSE;
        if (strchr(opts[i][1], ' '))
            blankExist = TRUE;
        if (blankExist)
        {
            *ppacket = '\'';
            ppacket++;
        }
        strcpy(ppacket, opts[i][1]);
        ppacket += strlen(opts[i][1]);
        if (blankExist)
        {
            *ppacket = '\'';
            ppacket++;
        }
    }
    if (self->login_timeout > 0)
    {
        sprintf(ppacket, " %s=", l_login_timeout);
        ppacket += (strlen(l_login_timeout) + 2);
        sprintf(ppacket, FORMAT_UINTEGER, self->login_timeout);
        ppacket = strchr(ppacket, '\0');
    }
    *ppacket = '\0';
inolog("return conninfo=%s(%d)\n", conninfo, strlen(conninfo));
    return conninfo;
}

static char CC_initial_log(ConnectionClass *self, const char *func)
{
    const ConnInfo  *ci = &self->connInfo;
    char    *encoding, vermsg[128];

    snprintf(vermsg, sizeof(vermsg), "Driver Version='%s,%s'"
#ifdef  WIN32
        " linking %d"
#ifdef  _MT
#ifdef  _DLL
        " dynamic"
#else
        " static"
#endif /* _DLL */
        " Multithread"
#else
        " Singlethread"
#endif /* _MT */
#ifdef  _DEBUG
        " Debug"
#endif /* DEBUG */
        " library"
#endif /* WIN32 */
        "\n", HSQLDRIVERVERSION, PG_BUILD_VERSION
#ifdef  _MSC_VER
        , _MSC_VER
#endif /* _MSC_VER */
        );
    qlog(vermsg);
    mylog(vermsg);
    qlog("Global Options: fetch=%d, socket=%d, unknown_sizes=%d, max_varchar_size=%d, max_longvarchar_size=%d\n",
         ci->drivers.fetch_max,
         ci->drivers.socket_buffersize,
         ci->drivers.unknown_sizes,
         ci->drivers.max_varchar_size,
         ci->drivers.max_longvarchar_size);
    qlog("                disable_optimizer=%d, ksqo=%d, unique_index=%d, use_declarefetch=%d\n",
         ci->drivers.disable_optimizer,
         ci->drivers.ksqo,
         ci->drivers.unique_index,
         ci->drivers.use_declarefetch);
    qlog("                text_as_longvarchar=%d, unknowns_as_longvarchar=%d, bools_as_char=%d NAMEDATALEN=%d\n",
         ci->drivers.text_as_longvarchar,
         ci->drivers.unknowns_as_longvarchar,
         ci->drivers.bools_as_char,
         TABLE_NAME_STORAGE_LEN);

    encoding = check_client_encoding(ci->conn_settings);
    if (encoding)
        self->original_client_encoding = encoding;
    else
    {
        encoding = check_client_encoding(ci->drivers.conn_settings);
        if (encoding)
            self->original_client_encoding = encoding;
    }
    if (self->original_client_encoding)
        self->ccsc = pg_CS_code(self->original_client_encoding);
    qlog("                extra_systable_prefixes='%s', conn_settings='%s' conn_encoding='%s'\n",
         ci->drivers.extra_systable_prefixes,
         ci->drivers.conn_settings,
         encoding ? encoding : "");
    if (self->status != CONN_NOT_CONNECTED)
    {
        CC_set_error(self, CONN_OPENDB_ERROR, "Already connected.", func);
        return 0;
    }

    mylog("%s: DSN = '%s', server = '%s', port = '%s', database = '%s', username = '%s', password='%s'\n", func, ci->dsn, ci->server, ci->port, ci->database, ci->username, ci->password ? "xxxxx" : "");

    if (ci->port[0] == '\0' ||
#ifdef  WIN32
        ci->server[0] == '\0' ||
#endif /* WIN32 */
        ci->database[0] == '\0')
    {
        CC_set_error(self, CONN_INIREAD_ERROR, "Missing server name, port, or database name in call to CC_connect.", func);
        return 0;
    }

    return 1;
}

static  char    CC_setenv(ConnectionClass *self);

static char
original_CC_connect(ConnectionClass *self, char password_req, char *salt_para)
{
    StartupPacket sp;
    StartupPacket6_2 sp62;
    QResultClass *res;
    SocketClass *sock;
    ConnInfo   *ci = &(self->connInfo);
    int         areq = -1;
    int         beresp, sockerr;
    char        msgbuffer[ERROR_MSG_LENGTH];
    char        salt[5], notice[512];
    CSTR        func = "original_CC_connect";
    BOOL    startPacketReceived = FALSE, anotherVersionRetry;
    BOOL        ReadyForQuery = FALSE, retry = FALSE;
    uint32  leng;

    mylog("%s: entering...\n", func);

    if (password_req != AUTH_REQ_OK)
    {
        sock = self->sock;      /* already connected, just authenticate */
        CC_clear_error(self);   
    }
    else
    {
        if (0 == CC_initial_log(self, func))
            return 0;

        anotherVersionRetry = FALSE;

another_version_retry:

        if (anotherVersionRetry)
        {
                strncpy_null(ci->protocol, PG64, sizeof(ci->protocol));
            if (self->sock)
            {
                SOCK_Destructor(self->sock);
                self->sock = (SocketClass *) 0;
            }
            self->status = CONN_NOT_CONNECTED;
            CC_initialize_pg_version(self);
            anotherVersionRetry = FALSE;
        }
        /*
         * If the socket was closed for some reason (like a SQLDisconnect,
         * but no SQLFreeConnect then create a socket now.
         */
        if (!self->sock)
        {
            self->sock = SOCK_Constructor(self);
            if (!self->sock)
            {
                CC_set_error(self, CONNECTION_SERVER_NOT_REACHED, "Could not construct a socket to the server", func);
                return 0;
            }
        }

        sock = self->sock;

        mylog("connecting to the server socket...\n");

        SOCK_connect_to(sock, (short) atoi(ci->port), ci->server, self->login_timeout);
        if (SOCK_get_errcode(sock) != 0)
        {
            CC_set_error(self, CONNECTION_SERVER_NOT_REACHED, "Could not connect to the server", func);
            return 0;
        }
        mylog("connection to the server socket succeeded.\n");

inolog("protocol=%s version=%d,%d\n", ci->protocol, self->pg_version_major, self->pg_version_minor);
        if (!protocol3_packet_build(self))
            return 0;

        if (SOCK_get_errcode(sock) != 0)
        {
            CC_set_error(self, CONN_INVALID_AUTHENTICATION, "Failed to send the authentication packet", func);
            return 0;
        }
        mylog("sent the authentication block successfully.\n");
    }


    mylog("gonna do authentication\n");


    /*
     * Now get the authentication request from backend
     */

    do
    {
        if (password_req != AUTH_REQ_OK)
        {
            beresp = 'R';
            startPacketReceived = TRUE;
        }
        else
        {
            beresp = SOCK_get_id(sock);
            mylog("auth got '%c'\n", beresp);
            if (0 != SOCK_get_errcode(sock))
                goto sockerr_proc;
            if (beresp != 'E' || startPacketReceived)
            {
                leng = SOCK_get_response_length(sock);
                inolog("leng=%d\n", leng);
                if (0 != SOCK_get_errcode(sock))
                    goto sockerr_proc;
            }
            startPacketReceived = TRUE;
        }

        switch (beresp)
        {
            case 'E':
inolog("Ekita retry=%d\n", retry);
                handle_error_message(self, msgbuffer, sizeof(msgbuffer), self->sqlstate, func, NULL);
                CC_set_error(self, CONN_INVALID_AUTHENTICATION, msgbuffer, func);
                qlog("ERROR from backend during authentication: '%s'\n", msgbuffer);
                if (0 == strncmp(msgbuffer, "FATAL:", 6))
                {
                    const char *emsg = msgbuffer + 8;
                    if (0 == strnicmp(emsg, "unsupported frontend protocol", 29))
                        retry = TRUE;
                }
                else if (strnicmp(msgbuffer, "Unsupported frontend protocol", 29) == 0)
                    retry = TRUE;
                if (retry)
                    break;

                return 0;
            case 'R':

                if (password_req != AUTH_REQ_OK)
                {
                    mylog("in 'R' password_req=%s\n", ci->password);
                    areq = password_req;
                    if (salt_para)
                        memcpy(salt, salt_para, sizeof(salt));
                    password_req = AUTH_REQ_OK;
                    mylog("salt=%02x%02x%02x%02x%02x\n", (UCHAR)salt[0], (UCHAR)salt[1], (UCHAR)salt[2], (UCHAR)salt[3], (UCHAR)salt[4]);
                }
                else
                {

                    areq = SOCK_get_int(sock, 4);
                    memset(salt, 0, sizeof(salt));
                    if (areq == AUTH_REQ_MD5)
                        SOCK_get_n_char(sock, salt, 4);
                    else if (areq == AUTH_REQ_CRYPT)
                        SOCK_get_n_char(sock, salt, 2);

                    mylog("areq = %d salt=%02x%02x%02x%02x%02x\n", areq, (UCHAR)salt[0], (UCHAR)salt[1], (UCHAR)salt[2], (UCHAR)salt[3], (UCHAR)salt[4]);
                }
                switch (areq)
                {
                    case AUTH_REQ_OK:
                        break;

                    case AUTH_REQ_KRB4:
                        CC_set_error(self, CONN_AUTH_TYPE_UNSUPPORTED, "Kerberos 4 authentication not supported", func);
                        return 0;

                    case AUTH_REQ_KRB5:
                        CC_set_error(self, CONN_AUTH_TYPE_UNSUPPORTED, "Kerberos 5 authentication not supported", func);
                        return 0;

                    case AUTH_REQ_PASSWORD:
                        mylog("in AUTH_REQ_PASSWORD\n");

                        if (ci->password[0] == '\0')
                        {
                            CC_set_error(self, CONNECTION_NEED_PASSWORD, "A password is required for this connection.", func);
                            return -areq;       /* need password */
                        }

                        mylog("past need password\n");

                        SOCK_put_char(sock, 'p');
                        SOCK_put_int(sock, (Int4) (4 + strlen(ci->password) + 1), 4);
                        SOCK_put_n_char(sock, ci->password, (Int4) strlen(ci->password) + 1);
                        sockerr = SOCK_flush_output(sock);

                        mylog("past flush %dbytes\n", sockerr);
                        break;

                    case AUTH_REQ_CRYPT:
                        CC_set_error(self, CONN_AUTH_TYPE_UNSUPPORTED, "Password crypt authentication not supported", func);
                        return 0;
                    case AUTH_REQ_MD5:
                        mylog("in AUTH_REQ_MD5\n");
                        if (ci->password[0] == '\0')
                        {
                            CC_set_error(self, CONNECTION_NEED_PASSWORD, "A password is required for this connection.", func);
                            if (salt_para)
                                memcpy(salt_para, salt, sizeof(salt));
                            return -areq; /* need password */
                        }
                        if (md5_auth_send(self, salt))
                        {
                            CC_set_error(self, CONN_INVALID_AUTHENTICATION, "md5 hashing failed", func);
                            return 0;
                        }
                        break;

                    case AUTH_REQ_SCM_CREDS:
                        CC_set_error(self, CONN_AUTH_TYPE_UNSUPPORTED, "Unix socket credential authentication not supported", func);
                        return 0;

                    default:
                        CC_set_error(self, CONN_AUTH_TYPE_UNSUPPORTED, "Unknown authentication type", func);
                        return 0;
                }
                break;
            case 'S': /* parameter status */
                getParameterValues(self);
                break;
            case 'K':       /* Secret key (6.4 protocol) */
                self->be_pid = SOCK_get_int(sock, 4);       /* pid */
                self->be_key = SOCK_get_int(sock, 4);       /* key */

                break;
            case 'Z':       /* Backend is ready for new query (6.4) */
                EatReadyForQuery(self);
                ReadyForQuery = TRUE;
                break;
            case 'N':   /* Notices may come */
                handle_notice_message(self, notice, sizeof(notice), self->sqlstate, "CC_connect", NULL);
                break;
            default:
                snprintf(notice, sizeof(notice), "Unexpected protocol character='%c' during authentication", beresp);
                CC_set_error(self, CONN_INVALID_AUTHENTICATION, notice, func);
                return 0;
        }
        if (retry)
        {
            anotherVersionRetry = TRUE;
            goto another_version_retry;
        }

    } while (!ReadyForQuery);

sockerr_proc:
    if (0 != (sockerr = SOCK_get_errcode(sock)))
    {
        if (0 == CC_get_errornumber(self))
        {
            if (SOCKET_CLOSED == sockerr)
                CC_set_error(self, CONN_INVALID_AUTHENTICATION, "Communication closed during authentication", func);
            else
                CC_set_error(self, CONN_INVALID_AUTHENTICATION, "Communication error during authentication", func);
        }
        return 0;
    }

    CC_clear_error(self);       /* clear any password error */

    return 1;
}   

char
CC_connect(ConnectionClass *self, char password_req, char *salt_para)
{
    ConnInfo *ci = &(self->connInfo);
    CSTR    func = "CC_connect";
    char        ret, *saverr = NULL;

    mylog("%s: entering...\n", func);

    mylog("sslmode=%s\n", self->connInfo.sslmode);
    {
        ret = original_CC_connect(self, password_req, salt_para);
    }
    if (ret <= 0)
        return ret;

    CC_set_translation(self);

    /*
     * Send any initial settings
     */

    /*
     * Since these functions allocate statements, and since the connection
     * is not established yet, it would violate odbc state transition
     * rules.  Therefore, these functions call the corresponding local
     * function instead.
     */
inolog("CC_send_settings\n");
    CC_send_settings(self);

    if (CC_get_errornumber(self) > 0)
        saverr = strdup(CC_get_errormsg(self));
    CC_clear_error(self);           /* clear any error */
    CC_lookup_lo(self);         /* a hack to get the oid of
                           our large object oid type */

    /*
     *  Multibyte handling is available ?
     */
    if (PG_VERSION_GE(self, 6.4))
    {
        CC_lookup_characterset(self);
        if (CC_get_errornumber(self) > 0)
        {
            ret = 0;
            goto cleanup;
        }
#ifdef UNICODE_SUPPORT
        if (CC_is_in_unicode_driver(self))
        {
            if (!self->original_client_encoding ||
                UTF8 != self->ccsc)
            {
                QResultClass    *res;
                if (self->original_client_encoding)
                    free(self->original_client_encoding);
                self->original_client_encoding = NULL;
                if (res = CC_send_query(self, "set client_encoding to 'UTF8'", NULL, 0, NULL), QR_command_maybe_successful(res))
                {
                    self->original_client_encoding = strdup("UNICODE");
                    self->ccsc = pg_CS_code(self->original_client_encoding);
                }
                QR_Destructor(res);
            }
        }
#else
        {
        }
#endif /* UNICODE_SUPPORT */
    }
#ifdef UNICODE_SUPPORT
    else if (CC_is_in_unicode_driver(self))
    {
        CC_set_error(self, CONN_NOT_IMPLEMENTED_ERROR, "Unicode isn't supported before 6.4", func);
        ret = 0;
        goto cleanup;
    }
#endif /* UNICODE_SUPPORT */
    ci->updatable_cursors = DISALLOW_UPDATABLE_CURSORS; 
    if (ci->allow_keyset &&
        PG_VERSION_GE(self, 7.0)) /* Tid scan since 7.0 */
    {
        if (ci->drivers.lie || !ci->drivers.use_declarefetch)
            ci->updatable_cursors |= (ALLOW_STATIC_CURSORS | ALLOW_KEYSET_DRIVEN_CURSORS | ALLOW_BULK_OPERATIONS | SENSE_SELF_OPERATIONS);
        else
        {
            if (PG_VERSION_GE(self, 7.4)) /* HOLDABLE CURSORS since 7.4 */
                ci->updatable_cursors |= (ALLOW_STATIC_CURSORS | SENSE_SELF_OPERATIONS);
        }
    }

    if (CC_get_errornumber(self) > 0)
        CC_clear_error(self);       /* clear any initial command errors */
    self->status = CONN_CONNECTED;
    if (CC_is_in_unicode_driver(self)
        && 0 < ci->bde_environment)
        self->unicode |= CONN_DISALLOW_WCHAR;
mylog("conn->unicode=%d\n", self->unicode);
    ret = 1;

cleanup:
    mylog("%s: returning...%d\n", func, ret);
    if (NULL != saverr)
    {
        if (ret > 0 && CC_get_errornumber(self) <= 0)
            CC_set_error(self, -1, saverr, func);
        free(saverr);
    }

    return ret;
}


char
CC_add_statement(ConnectionClass *self, StatementClass *stmt)
{
    int i;
    char    ret = TRUE;

    mylog("CC_add_statement: self=%p, stmt=%p\n", self, stmt);

    CONNLOCK_ACQUIRE(self);
    for (i = 0; i < self->num_stmts; i++)
    {
        if (!self->stmts[i])
        {
            stmt->hdbc = self;
            self->stmts[i] = stmt;
            break;
        }
    }

    if (i >= self->num_stmts) /* no more room -- allocate more memory */
    {
        self->stmts = (StatementClass **) realloc(self->stmts, sizeof(StatementClass *) * (STMT_INCREMENT + self->num_stmts));
        if (!self->stmts)
            ret = FALSE;
        else
        {
            memset(&self->stmts[self->num_stmts], 0, sizeof(StatementClass *) * STMT_INCREMENT);

            stmt->hdbc = self;
            self->stmts[self->num_stmts] = stmt;

            self->num_stmts += STMT_INCREMENT;
        }
    }
    CONNLOCK_RELEASE(self);

    return TRUE;
}

static void
CC_set_error_statements(ConnectionClass *self)
{
    int i;

    mylog("CC_error_statements: self=%p\n", self);

    for (i = 0; i < self->num_stmts; i++)
    {
        if (NULL != self->stmts[i])
            SC_ref_CC_error(self->stmts[i]);
    }
}


char
CC_remove_statement(ConnectionClass *self, StatementClass *stmt)
{
    int i;
    char    ret = FALSE;

    CONNLOCK_ACQUIRE(self);
    for (i = 0; i < self->num_stmts; i++)
    {
        if (self->stmts[i] == stmt && stmt->status != STMT_EXECUTING)
        {
            self->stmts[i] = NULL;
            ret = TRUE;
            break;
        }
    }
    CONNLOCK_RELEASE(self);

    return ret;
}

int CC_get_max_idlen(ConnectionClass *self)
{
    int len = self->max_identifier_length;

    if  (len < 0)
    {
        QResultClass    *res;

        res = CC_send_query(self, "show max_identifier_length", NULL, ROLLBACK_ON_ERROR | IGNORE_ABORT_ON_CONN, NULL);
        if (QR_command_maybe_successful(res))
            len = self->max_identifier_length = atoi(res->command);
        QR_Destructor(res);
    }
mylog("max_identifier_length=%d\n", len);
    return len < 0 ? 0 : len; 
}

/*
 *  Create a more informative error message by concatenating the connection
 *  error message with its socket error message.
 */
static char *
CC_create_errormsg(ConnectionClass *self)
{
    SocketClass *sock = self->sock;
    size_t  pos;
    char    msg[4096];
    const char *sockerrmsg;

    mylog("enter CC_create_errormsg\n");

    msg[0] = '\0';

    if (CC_get_errormsg(self))
        strncpy_null(msg, CC_get_errormsg(self), sizeof(msg));

    mylog("msg = '%s'\n", msg);

    if (sock && NULL != (sockerrmsg = SOCK_get_errmsg(sock)) && '\0' != sockerrmsg[0])
    {
        pos = strlen(msg);
        snprintf(&msg[pos], sizeof(msg) - pos, ";\n%s", sockerrmsg);
    }

    mylog("exit CC_create_errormsg\n");
    return strdup(msg);
}


void
CC_set_error(ConnectionClass *self, int number, const char *message, const char *func)
{
    CONNLOCK_ACQUIRE(self);
    if (self->__error_message)
        free(self->__error_message);
    self->__error_number = number;
    self->__error_message = message ? strdup(message) : NULL;
    if (0 != number)
        CC_set_error_statements(self);
    if (func && number != 0)
        CC_log_error(func, "", self); 
    CONNLOCK_RELEASE(self);
}


void
CC_set_errormsg(ConnectionClass *self, const char *message)
{
    CONNLOCK_ACQUIRE(self);
    if (self->__error_message)
        free(self->__error_message);
    self->__error_message = message ? strdup(message) : NULL;
    CONNLOCK_RELEASE(self);
}


char
CC_get_error(ConnectionClass *self, int *number, char **message)
{
    int         rv;
    char *msgcrt;

    mylog("enter CC_get_error\n");

    CONNLOCK_ACQUIRE(self);
    /* Create a very informative errormsg if it hasn't been done yet. */
    if (!self->errormsg_created)
    {
        msgcrt = CC_create_errormsg(self);
        if (self->__error_message)
            free(self->__error_message);
        self->__error_message = msgcrt;
        self->errormsg_created = TRUE;
    }

    if (CC_get_errornumber(self))
    {
        *number = CC_get_errornumber(self);
        *message = CC_get_errormsg(self);
    }
    rv = (CC_get_errornumber(self) != 0);

    self->__error_number = 0;       /* clear the error */
    CONNLOCK_RELEASE(self);

    mylog("exit CC_get_error\n");

    return rv;
}


static void CC_clear_cursors(ConnectionClass *self, BOOL on_abort)
{
    int i;
    StatementClass  *stmt;
    QResultClass    *res;

    if (!self->ncursors)
        return;
    CONNLOCK_ACQUIRE(self);
    for (i = 0; i < self->num_stmts; i++)
    {
        stmt = self->stmts[i];
        if (stmt && (res = SC_get_Result(stmt)) &&
             (NULL != QR_get_cursor(res)))
        {
            if ((on_abort && !QR_is_permanent(res)) ||
                !QR_is_withhold(res))
            /*
             * non-holdable cursors are automatically closed
             * at commit time.
             * all non-permanent cursors are automatically closed
             * at rollback time.
             */ 
                QR_set_cursor(res, NULL);
            else if (!QR_is_permanent(res))
            {
                QResultClass    *wres;
                char    cmd[64];

                snprintf(cmd, sizeof(cmd), "MOVE 0 in \"%s\"", QR_get_cursor(res));
                CONNLOCK_RELEASE(self);
                wres = CC_send_query(self, cmd, NULL, ROLLBACK_ON_ERROR | IGNORE_ABORT_ON_CONN, NULL);
                if (QR_command_maybe_successful(wres))
                    QR_set_permanent(res);
                else
                    QR_set_cursor(res, NULL);
                QR_Destructor(wres);
                CONNLOCK_ACQUIRE(self);
            }
        }
    }
    CONNLOCK_RELEASE(self);
}
    
void    CC_on_commit(ConnectionClass *conn)
{
    CONNLOCK_ACQUIRE(conn);
    if (CC_is_in_trans(conn))
    {
        CC_set_no_trans(conn);
        CC_set_no_manual_trans(conn);
    }
    CC_clear_cursors(conn, FALSE);
    CONNLOCK_RELEASE(conn);
    CC_discard_marked_objects(conn);
    CONNLOCK_ACQUIRE(conn);
    if (conn->result_uncommitted)
    {
        CONNLOCK_RELEASE(conn);
        ProcessRollback(conn, FALSE, FALSE);
        CONNLOCK_ACQUIRE(conn);
        conn->result_uncommitted = 0;
    }
    CONNLOCK_RELEASE(conn);
}
void    CC_on_abort(ConnectionClass *conn, UDWORD opt)
{
    BOOL    set_no_trans = FALSE;

mylog("CC_on_abort in\n");
    CONNLOCK_ACQUIRE(conn);
    if (0 != (opt & CONN_DEAD)) /* CONN_DEAD implies NO_TRANS also */
        opt |= NO_TRANS;
    if (CC_is_in_trans(conn))
    {
        if (0 != (opt & NO_TRANS))
        {
            CC_set_no_trans(conn);
            CC_set_no_manual_trans(conn);
            set_no_trans = TRUE;
        }
    }
    CC_clear_cursors(conn, TRUE);
    if (0 != (opt & CONN_DEAD))
    {
        conn->status = CONN_DOWN;
        if (conn->sock)
        {
            CONNLOCK_RELEASE(conn);
            SOCK_Destructor(conn->sock);
            CONNLOCK_ACQUIRE(conn);
            conn->sock = NULL;
        }
    }
    else if (set_no_trans)
    {
        CONNLOCK_RELEASE(conn);
        CC_discard_marked_objects(conn);
        CONNLOCK_ACQUIRE(conn);
    }
    if (conn->result_uncommitted)
    {
        CONNLOCK_RELEASE(conn);
        ProcessRollback(conn, TRUE, FALSE);
        CONNLOCK_ACQUIRE(conn);
        conn->result_uncommitted = 0;
    }
    CONNLOCK_RELEASE(conn);
}

void    CC_on_abort_partial(ConnectionClass *conn)
{
mylog("CC_on_abort_partial in\n");
    ProcessRollback(conn, TRUE, TRUE);
    CONNLOCK_ACQUIRE(conn);
    CC_discard_marked_objects(conn);
    CONNLOCK_RELEASE(conn);
}

static BOOL
is_setting_search_path(const UCHAR* query)
{
    for (query += 4; *query; query++)
    {
        if (!isspace(*query))
        {
            if (strnicmp(query, "search_path", 11) == 0)
                return TRUE;
            query++;
            while (*query && !isspace(*query))
                query++;
        }
    }
    return FALSE;
}

/*
 *  The "result_in" is only used by QR_next_tuple() to fetch another group of rows into
 *  the same existing QResultClass (this occurs when the tuple cache is depleted and
 *  needs to be re-filled).
 *
 *  The "cursor" is used by SQLExecute to associate a statement handle as the cursor name
 *  (i.e., C3326857) for SQL select statements.  This cursor is then used in future
 *  'declare cursor C3326857 for ...' and 'fetch 100 in C3326857' statements.
 */
QResultClass *
CC_send_query_append(ConnectionClass *self, const char *query, QueryInfo *qi, UDWORD flag, StatementClass *stmt, const char *appendq)
{
    CSTR    func = "CC_send_query";
    QResultClass *cmdres = NULL,
               *retres = NULL,
               *res = NULL;
    BOOL    ignore_abort_on_conn = ((flag & IGNORE_ABORT_ON_CONN) != 0),
        create_keyset = ((flag & CREATE_KEYSET) != 0),
        issue_begin = ((flag & GO_INTO_TRANSACTION) != 0 && !CC_is_in_trans(self)),
        rollback_on_error, query_rollback, end_with_commit;

    const char  *wq;
    char        swallow, *ptr;
    CSTR    svpcmd = "SAVEPOINT";
    CSTR    per_query_svp = "_per_query_svp_";
    CSTR    rlscmd = "RELEASE";
    static size_t   lenbgncmd = 0, lenrbkcmd = 0, lensvpcmd = 0,
            lenrlscmd = 0, lenperqsvp = 0;
    size_t  qrylen;
    int         id;
    SocketClass *sock = self->sock;
    int         maxlen,
                empty_reqs;
    BOOL        msg_truncated,
                ReadyToReturn = FALSE,
                query_completed = FALSE,
                aborted = FALSE,
                used_passed_result_object = FALSE,
            discard_next_begin = FALSE,
            discard_next_savepoint = FALSE,
            consider_rollback;
    Int4        response_length;
    UInt4       leng;
    ConnInfo    *ci = &(self->connInfo);
    int     func_cs_count = 0;

    /* ERROR_MSG_LENGTH is suffcient */
    char msgbuffer[ERROR_MSG_LENGTH + 1];

    /* QR_set_command() dups this string so doesn't need static */
    char        cmdbuffer[ERROR_MSG_LENGTH + 1];
    BOOL        reduce_round_trip_time = !(flag & IGNORE_ROUND_TRIP);

    if (appendq)
    {
        mylog("%s_append: conn=%p, query='%s'+'%s'\n", func, self, query, appendq);
        qlog("conn=%p, query='%s'+'%s'\n", self, query, appendq);
    }
    else
    {
        mylog("%s: conn=%p, query='%s'\n", func, self, query);
        qlog("conn=%p, query='%s'\n", self, query);
    }

    if (!self->sock)
    {
        CC_set_error(self, CONNECTION_COULD_NOT_SEND, "Could not send Query(connection dead)", func);
        CC_on_abort(self, CONN_DEAD);
        return NULL;
    }

    /* Indicate that we are sending a query to the backend */
    maxlen = CC_get_max_query_len(self);
    qrylen = strlen(query);
    if (maxlen > 0 && maxlen < (int) qrylen + 1)
    {
        CC_set_error(self, CONNECTION_MSG_TOO_LONG, "Query string is too long", func);
        return NULL;
    }

    if ((NULL == query) || (query[0] == '\0'))
        return NULL;

    if (SOCK_get_errcode(sock) != 0)
    {
        CC_set_error(self, CONNECTION_COULD_NOT_SEND, "Could not send Query to backend", func);
        CC_on_abort(self, CONN_DEAD);
        return NULL;
    }

    /*
     *  In case the round trip time can be ignored, the query
     *  and the appeneded query would be issued separately.
     *  Otherwise a multiple command query would be issued.
     */ 
    if (appendq && !reduce_round_trip_time)
    {
        res = CC_send_query_append(self, query, qi, flag, stmt, NULL);
        if (QR_command_maybe_successful(res))
        {
            cmdres = CC_send_query_append(self, appendq, qi, flag & (~(GO_INTO_TRANSACTION)), stmt, NULL);
            if (QR_command_maybe_successful(cmdres))
                res->next = cmdres;
            else
            {
                QR_Destructor(res);
                res = cmdres;
            }
        }
        return res;
    }

    rollback_on_error = (flag & ROLLBACK_ON_ERROR) != 0;
    end_with_commit = (flag & END_WITH_COMMIT) != 0;
#define return DONT_CALL_RETURN_FROM_HERE???
    ENTER_INNER_CONN_CS(self, func_cs_count);
    consider_rollback = (issue_begin || (CC_is_in_trans(self) && !CC_is_in_error_trans(self)) || strnicmp(query, "begin", 5) == 0);
    if (rollback_on_error)
        rollback_on_error = consider_rollback;
    query_rollback = (rollback_on_error && !end_with_commit && PG_VERSION_GE(self, 8.0));
    if (!query_rollback && consider_rollback && !end_with_commit)
    {
        if (stmt)
        {
            StatementClass  *astmt = SC_get_ancestor(stmt);
            if (!SC_accessed_db(astmt))
            {
                if (SQL_ERROR == SetStatementSvp(astmt))
                {
                    SC_set_error(stmt, STMT_INTERNAL_ERROR, "internal savepoint error", func);
                    goto cleanup;
                }
            }
        }
    }

    SOCK_put_char(sock, 'Q');
    if (SOCK_get_errcode(sock) != 0)
    {
        CC_set_error(self, CONNECTION_COULD_NOT_SEND, "Could not send Query to backend", func);
        CC_on_abort(self, CONN_DEAD);
        goto cleanup;
    }
    if (stmt)
        SC_forget_unnamed(stmt);

    if (!lenbgncmd)
    {
        lenbgncmd = strlen(bgncmd);
        lensvpcmd = strlen(svpcmd);
        lenrbkcmd = strlen(rbkcmd);
        lenrlscmd = strlen(rlscmd);
        lenperqsvp = strlen(per_query_svp);
    }
    leng = (UInt4) qrylen;
    if (appendq)
        leng += (UInt4) (strlen(appendq) + 1);
    if (issue_begin)
        leng += (UInt4) (lenbgncmd + 1);
    if (query_rollback)
    {
        leng += (UInt4) (lensvpcmd + 1 + lenperqsvp + 1);
        leng += (UInt4) (1 + lenrlscmd + 1 + lenperqsvp);
    }
    leng++;
    SOCK_put_int(sock, leng + 4, 4);
inolog("leng=%d\n", leng);
    if (issue_begin)
    {
        SOCK_put_n_char(sock, bgncmd, lenbgncmd);
        SOCK_put_n_char(sock, semi_colon, 1);
        discard_next_begin = TRUE;
    }
    if (query_rollback)
    {
        char cmd[64];

        snprintf(cmd, sizeof(cmd), "%s %s;", svpcmd, per_query_svp);
        SOCK_put_n_char(sock, cmd, (Int4) strlen(cmd));
        discard_next_savepoint = TRUE;
    }
    SOCK_put_n_char(sock, query, qrylen);
    if (appendq)
    {
        SOCK_put_n_char(sock, semi_colon, 1);
        SOCK_put_n_char(sock, appendq, strlen(appendq));
    }
    if (query_rollback)
    {
        char cmd[64];

        snprintf(cmd, sizeof(cmd), ";%s %s", rlscmd, per_query_svp);
        SOCK_put_n_char(sock, cmd, (Int4) strlen(cmd));
    }
    SOCK_put_n_char(sock, NULL_STRING, 1);
    leng = SOCK_flush_output(sock);

    if (SOCK_get_errcode(sock) != 0)
    {
        CC_set_error(self, CONNECTION_COULD_NOT_SEND, "Could not send Query to backend", func);
        CC_on_abort(self, CONN_DEAD);
        goto cleanup;
    }

    mylog("send_query: done sending query %dbytes flushed\n", leng);
 
    empty_reqs = 0;
    for (wq = query; isspace((UCHAR) *wq); wq++)
        ;
    if (*wq == '\0')
        empty_reqs = 1;
    cmdres = qi ? qi->result_in : NULL;
    if (cmdres)
        used_passed_result_object = TRUE;
    else
    {
        cmdres = QR_Constructor();
        if (!cmdres)
        {
            CC_set_error(self, CONNECTION_COULD_NOT_RECEIVE, "Could not create result info in send_query.", func);
            goto cleanup;
        }
    }
    res = cmdres;
    while (!ReadyToReturn)
    {
        /* what type of message is coming now ? */
        id = SOCK_get_id(sock);

        if ((SOCK_get_errcode(sock) != 0) || (id == EOF))
        {
            CC_set_error(self, CONNECTION_NO_RESPONSE, "No response from the backend", func);

            mylog("send_query: 'id' - %s\n", CC_get_errormsg(self));
            CC_on_abort(self, CONN_DEAD);
            ReadyToReturn = TRUE;
            retres = NULL;
            break;
        }

        mylog("send_query: got id = '%c'\n", id);

        response_length = SOCK_get_response_length(sock);
inolog("send_query response_length=%d\n", response_length);
        switch (id)
        {
            case 'A':           /* Asynchronous Messages are ignored */
                (void) SOCK_get_int(sock, 4);   /* id of notification */
                SOCK_get_string(sock, msgbuffer, ERROR_MSG_LENGTH);
                /* name of the relation the message comes from */
                break;
            case 'C':           /* portal query command, no tuples
                                 * returned */
                /* read in the return message from the backend */
                SOCK_get_string(sock, cmdbuffer, ERROR_MSG_LENGTH);
                if (SOCK_get_errcode(sock) != 0)
                {
                    CC_set_error(self, CONNECTION_NO_RESPONSE, "No response from backend while receiving a portal query command", func);
                    mylog("send_query: 'C' - %s\n", CC_get_errormsg(self));
                    CC_on_abort(self, CONN_DEAD);
                    ReadyToReturn = TRUE;
                    retres = NULL;
                }
                else
                {
                    mylog("send_query: ok - 'C' - %s\n", cmdbuffer);

                    if (query_completed)    /* allow for "show" style notices */
                    {
                        res->next = QR_Constructor();
                        res = res->next;
                    } 

                    mylog("send_query: setting cmdbuffer = '%s'\n", cmdbuffer);

                    my_trim(cmdbuffer); /* get rid of trailing space */ 
                    if (strnicmp(cmdbuffer, bgncmd, lenbgncmd) == 0)
                    {
                        CC_set_in_trans(self);
                        if (discard_next_begin) /* disicard the automatically issued BEGIN */
                        {
                            discard_next_begin = FALSE;
                            continue; /* discard the result */
                        }
                    }
                    else if (strnicmp(cmdbuffer, svpcmd, lensvpcmd) == 0)
                    {
                        if (discard_next_savepoint)
                        {
inolog("Discarded the first SAVEPOINT\n");
                            discard_next_savepoint = FALSE;
                            continue; /* discard the result */
                        }
                    }
                    else if (strnicmp(cmdbuffer, rbkcmd, lenrbkcmd) == 0)
                    {
                        CC_set_in_error_trans(self); /* mark the transaction error in case of manual rollback */
                    }
                    else
                    {
                        ptr = strrchr(cmdbuffer, ' ');
                        if (ptr)
                            res->recent_processed_row_count = atoi(ptr + 1);
                        else
                            res->recent_processed_row_count = -1;
                        if (NULL != self->current_schema &&
                            strnicmp(cmdbuffer, "SET", 3) == 0)
                        {
                            if (is_setting_search_path(query))
                                reset_current_schema(self);
                        }
                    }

                    if (QR_command_successful(res))
                        QR_set_rstatus(res, PORES_COMMAND_OK);
                    QR_set_command(res, cmdbuffer);
                    query_completed = TRUE;
                    mylog("send_query: returning res = %p\n", res);
                }
                break;
            case 'Z':           /* Backend is ready for new query (6.4) */
                if (empty_reqs == 0)
                {
                    ReadyToReturn = TRUE;
                    if (aborted || query_completed)
                        retres = cmdres;
                    else
                        ReadyToReturn = FALSE;
                }
                EatReadyForQuery(self);
                break;
            case 'N':           /* NOTICE: */
                msg_truncated = handle_notice_message(self, cmdbuffer, sizeof(cmdbuffer), res->sqlstate, "send_query", res);
                break;      /* dont return a result -- continue
                                 * reading */

            case 'I':           /* The server sends an empty query */
                /* There is a closing '\0' following the 'I', so we eat it */
                if (0 == response_length)
                    swallow = '\0';
                else
                    swallow = SOCK_get_char(sock);
                if ((swallow != '\0') || SOCK_get_errcode(sock) != 0)
                {
                    CC_set_errornumber(self, CONNECTION_BACKEND_CRAZY);
                    QR_set_message(res, "Unexpected protocol character from backend (send_query - I)");
                    QR_set_rstatus(res, PORES_FATAL_ERROR);
                    ReadyToReturn = TRUE;
                    retres = cmdres;
                    break;
                }
                else
                {
                    /* We return the empty query */
                    QR_set_rstatus(res, PORES_EMPTY_QUERY);
                }
                if (empty_reqs > 0)
                {
                    if (--empty_reqs == 0)
                        query_completed = TRUE;
                }
                break;
            case 'E':
                msg_truncated = handle_error_message(self, msgbuffer, sizeof(msgbuffer), res->sqlstate, "send_query", res);

                /* We should report that an error occured. Zoltan */
                aborted = TRUE;

                query_completed = TRUE;
                break;

            case 'P':           /* get the Portal name */
                SOCK_get_string(sock, msgbuffer, ERROR_MSG_LENGTH);
                break;
            case 'T':           /* Tuple results start here */
                if (query_completed)
                {
                    res->next = QR_Constructor();
                    if (!res->next)
                    {
                        CC_set_error(self, CONNECTION_COULD_NOT_RECEIVE, "Could not create result info in send_query.", func);
                        ReadyToReturn = TRUE;
                        retres = NULL;
                        break;
                    }
                    if (create_keyset)
                    {
                        QR_set_haskeyset(res->next);
                        if (stmt)
                            res->num_key_fields = stmt->num_key_fields;
                    }
                    mylog("send_query: 'T' no result_in: res = %p\n", res->next);
                    res = res->next;

                    if (qi)
                        QR_set_cache_size(res, qi->row_size);
                }
                if (!used_passed_result_object)
                {
                    const char *cursor = qi ? qi->cursor : NULL;
                    if (create_keyset)
                    {
                        QR_set_haskeyset(res);
                        if (stmt)
                            res->num_key_fields = stmt->num_key_fields;
                        if (cursor && cursor[0])
                            QR_set_synchronize_keys(res);
                    }
                    if (!QR_fetch_tuples(res, self, cursor))
                    {
                        CC_set_error(self, CONNECTION_COULD_NOT_RECEIVE, QR_get_message(res), func);
                        ReadyToReturn = TRUE;
                        if (PORES_FATAL_ERROR == QR_get_rstatus(res))
                            retres = cmdres;
                        else
                            retres = NULL;
                        break;
                    }
                    query_completed = TRUE;
                }
                else
                {               /* next fetch, so reuse an existing result */

                    /*
                     * called from QR_next_tuple and must return
                     * immediately.
                     */
                    ReadyToReturn = TRUE;
                    if (!QR_fetch_tuples(res, NULL, NULL))
                    {
                        CC_set_error(self, CONNECTION_COULD_NOT_RECEIVE, QR_get_message(res), func);
                        retres = NULL;
                        break;
                    }
                    retres = cmdres;
                }
                break;
            case 'D':           /* Copy in command began successfully */
                if (query_completed)
                {
                    res->next = QR_Constructor();
                    res = res->next;
                }
                QR_set_rstatus(res, PORES_COPY_IN);
                ReadyToReturn = TRUE;
                retres = cmdres;
                break;
            case 'B':           /* Copy out command began successfully */
                if (query_completed)
                {
                    res->next = QR_Constructor();
                    res = res->next;
                }
                QR_set_rstatus(res, PORES_COPY_OUT);
                ReadyToReturn = TRUE;
                retres = cmdres;
                break;
            case 'S':       /* parameter status */
                getParameterValues(self);
                break;
            case 's':       /* portal suspended */
                QR_set_no_fetching_tuples(res);
                break;
            default:
                /* skip the unexpected response if possible */
                if (response_length >= 0)
                    break;
                CC_set_error(self, CONNECTION_BACKEND_CRAZY, "Unexpected protocol character from backend (send_query)", func);
                CC_on_abort(self, CONN_DEAD);

                mylog("send_query: error - %s\n", CC_get_errormsg(self));
                ReadyToReturn = TRUE;
                retres = NULL;
                break;
        }

        if (SOCK_get_errcode(sock) != 0)
            break;
        if (CONN_DOWN == self->status)
            break;
    }

cleanup:
    if (SOCK_get_errcode(sock) != 0)
    {
        if (0 == CC_get_errornumber(self))
            CC_set_error(self, CONNECTION_COMMUNICATION_ERROR, "Communication error while sending query", func);
        CC_on_abort(self, CONN_DEAD);
        ReadyToReturn = TRUE;
        retres = NULL;
    }
    if (rollback_on_error && CC_is_in_trans(self) && !discard_next_savepoint)
    {
        char    cmd[64];

        cmd[0] = '\0'; 
        if (query_rollback)
        {
            if (CC_is_in_error_trans(self))
            {
                snprintf(cmd, sizeof(cmd), "%s TO %s;", rbkcmd, per_query_svp);
                snprintf_add(cmd, sizeof(cmd), "%s %s", rlscmd, per_query_svp);
            }
        }
        else if (CC_is_in_error_trans(self))
            strcpy(cmd, rbkcmd);
        if (cmd[0])
            QR_Destructor(CC_send_query(self, cmd, NULL, IGNORE_ABORT_ON_CONN, NULL));
    }

    CLEANUP_FUNC_CONN_CS(func_cs_count, self);
#undef  return
    /*
     * Break before being ready to return.
     */
    if (!ReadyToReturn)
        retres = cmdres;

    /*
     * Cleanup garbage results before returning.
     */
    if (cmdres && retres != cmdres && !used_passed_result_object)
        QR_Destructor(cmdres);
    /*
     * Cleanup the aborted result if specified
     */
    if (retres)
    {
        if (aborted)
        {
            /** if (ignore_abort_on_conn)
            {
                if (!used_passed_result_object)
                {
                    QR_Destructor(retres);
                    retres = NULL;
                }
            } **/
            if (retres)
            {
                /*
                 *  discard results other than errors.
                 */
                QResultClass    *qres;
                for (qres = retres; qres->next; qres = retres)
                {
                    if (QR_get_aborted(qres))
                        break;
                    retres = qres->next;
                    qres->next = NULL;
                    QR_Destructor(qres);
                }
                /*
                 *  If error message isn't set
                 */
                if (ignore_abort_on_conn)
                    CC_set_errornumber(self, 0);
                else if (retres)
                {
                    if (NULL == CC_get_errormsg(self) ||
                        !CC_get_errormsg(self)[0])
                        CC_set_errormsg(self, QR_get_message(retres));
                    if (!self->sqlstate[0])
                        strcpy(self->sqlstate, retres->sqlstate);
                }
            }
        }
    }
    return retres;
}


int
CC_send_function(ConnectionClass *self, int fnid, void *result_buf, int *actual_result_len, int result_is_int, LO_ARG *args, int nargs)
{
    // Wed Feb  4 18:19:52 EST 2009  This function disabled by Blaine
    // See Subversion rev. 994 for the contents of this function.
    CSTR    func = "CC_send_function";
    mylog("PURPOSEFULLY ABORTING send_function(): conn=%p, fnid=%d, result_is_int=%d, nargs=%d\n", self, fnid, result_is_int, nargs);
    CC_set_error(self, CONNECTION_COULD_NOT_SEND, "function capability disabled", func);
    CC_on_abort(self, CONN_DEAD);
    return FALSE;
}


static  char
CC_setenv(ConnectionClass *self)
{
    ConnInfo   *ci = &(self->connInfo);

    HSTMT       hstmt;
    StatementClass *stmt;
    RETCODE     result;
    char        status = TRUE;
    CSTR func = "CC_setenv";


    mylog("%s: entering...\n", func);

/*
 *  This function must use the local odbc API functions since the odbc state
 *  has not transitioned to "connected" yet.
 */

    result = PGAPI_AllocStmt(self, &hstmt);
    if (!SQL_SUCCEEDED(result))
        return FALSE;
    stmt = (StatementClass *) hstmt;

    stmt->internal = TRUE;      /* ensure no BEGIN/COMMIT/ABORT stuff */

    /* Set the Datestyle to the format the driver expects it to be in */
    result = PGAPI_ExecDirect(hstmt, "set DateStyle to 'ISO'", SQL_NTS, 0);
    if (!SQL_SUCCEEDED(result))
        status = FALSE;

    mylog("%s: result %d, status %d from set DateStyle\n", func, result, status);
    /* Disable genetic optimizer based on global flag */
    if (ci->drivers.disable_optimizer)
    {
        result = PGAPI_ExecDirect(hstmt, "set geqo to 'OFF'", SQL_NTS, 0);
        if (!SQL_SUCCEEDED(result))
            status = FALSE;

        mylog("%s: result %d, status %d from set geqo\n", func, result, status);

    }

    /* KSQO (not applicable to 7.1+ - DJP 21/06/2002) */
    if (ci->drivers.ksqo && PG_VERSION_LT(self, 7.1))
    {
        result = PGAPI_ExecDirect(hstmt, "set ksqo to 'ON'", SQL_NTS, 0);
        if (!SQL_SUCCEEDED(result))
            status = FALSE;

        mylog("%s: result %d, status %d from set ksqo\n", func, result, status);

    }

    /* extra_float_digits (applicable since 7.4) */
    if (PG_VERSION_GT(self, 7.3))
    {
        result = PGAPI_ExecDirect(hstmt, "set extra_float_digits to 2", SQL_NTS, 0);
        if (!SQL_SUCCEEDED(result))
            status = FALSE;

        mylog("%s: result %d, status %d from set extra_float_digits\n", func, result, status);

    }

    PGAPI_FreeStmt(hstmt, SQL_DROP);

    return status;
}

char
CC_send_settings(ConnectionClass *self)
{
    /* char ini_query[MAX_MESSAGE_LEN]; */
    ConnInfo   *ci = &(self->connInfo);

/* QResultClass *res; */
    HSTMT       hstmt;
    StatementClass *stmt;
    RETCODE     result;
    char        status = TRUE;
    char       *cs,
               *ptr;
#ifdef  HAVE_STRTOK_R
    char    *last;
#endif /* HAVE_STRTOK_R */
    CSTR func = "CC_send_settings";


    mylog("%s: entering...\n", func);

/*
 *  This function must use the local odbc API functions since the odbc state
 *  has not transitioned to "connected" yet.
 */

    result = PGAPI_AllocStmt(self, &hstmt);
    if (!SQL_SUCCEEDED(result))
        return FALSE;
    stmt = (StatementClass *) hstmt;

    stmt->internal = TRUE;      /* ensure no BEGIN/COMMIT/ABORT stuff */

    /* Global settings */
    if (ci->drivers.conn_settings[0] != '\0')
    {
        cs = strdup(ci->drivers.conn_settings);
#ifdef  HAVE_STRTOK_R
        ptr = strtok_r(cs, semi_colon, &last);
#else
        ptr = strtok(cs, semi_colon);
#endif /* HAVE_STRTOK_R */
        while (ptr)
        {
            result = PGAPI_ExecDirect(hstmt, ptr, SQL_NTS, 0);
            if (!SQL_SUCCEEDED(result))
                status = FALSE;

            mylog("%s: result %d, status %d from '%s'\n", func, result, status, ptr);

#ifdef  HAVE_STRTOK_R
            ptr = strtok_r(NULL, semi_colon, &last);
#else
            ptr = strtok(NULL, semi_colon);
#endif /* HAVE_STRTOK_R */
        }

        free(cs);
    }

    /* Per Datasource settings */
    if (ci->conn_settings[0] != '\0')
    {
        cs = strdup(ci->conn_settings);
#ifdef  HAVE_STRTOK_R
        ptr = strtok_r(cs, semi_colon, &last);
#else
        ptr = strtok(cs, semi_colon);
#endif /* HAVE_STRTOK_R */
        while (ptr)
        {
            result = PGAPI_ExecDirect(hstmt, ptr, SQL_NTS, 0);
            if (!SQL_SUCCEEDED(result))
                status = FALSE;

            mylog("%s: result %d, status %d from '%s'\n", func, result, status, ptr);

#ifdef  HAVE_STRTOK_R
            ptr = strtok_r(NULL, semi_colon, &last);
#else
            ptr = strtok(NULL, semi_colon);
#endif /* HAVE_STRTOK_R */
        }

        free(cs);
    }


    PGAPI_FreeStmt(hstmt, SQL_DROP);

    return status;
}


/*
 *  This function is just a hack to get the oid of our Large Object oid type.
 *  If a real Large Object oid type is made part of Postgres, this function
 *  will go away and the define 'PG_TYPE_LO' will be updated.
 *
 *  More specifically, the goal is to set the values
 *    self->lobj_type and self->lo_is_domain.
 */
static void
CC_lookup_lo(ConnectionClass *self)
{
    CSTR func = "CC_lookup_lo";
    mylog("%s: Stubbing.  TODO:  Implement LO support.\n", func);
    return;
}


/*
 *  This function initializes the version of PostgreSQL from
 *  connInfo.protocol that we're connected to.
 *  h-inoue 01-2-2001
 */
void
CC_initialize_pg_version(ConnectionClass *self)
{
    strcpy(self->pg_version, self->connInfo.protocol);
    self->pg_version_number = (float) 7.4;
    self->pg_version_major = 7;
    self->pg_version_minor = 4;
}


/*
 *  This function gets the version of PostgreSQL that we're connected to.
 *  This is used to return the correct info in SQLGetInfo
 *  DJP - 25-1-2001
 */
static void
CC_lookup_pg_version(ConnectionClass *self)
{
    HSTMT       hstmt;
    StatementClass *stmt;
    RETCODE     result;
    char        szVersion[32];
    int         major,
                minor;
    CSTR        func = "CC_lookup_pg_version";

    mylog("%s: entering...\n", func);

/*
 *  This function must use the local odbc API functions since the odbc state
 *  has not transitioned to "connected" yet.
 */
    result = PGAPI_AllocStmt(self, &hstmt);
    if (!SQL_SUCCEEDED(result))
        return;
    stmt = (StatementClass *) hstmt;

    /* get the server's version if possible  */
    result = PGAPI_ExecDirect(hstmt, "select version()", SQL_NTS, 0);
    if (!SQL_SUCCEEDED(result))
    {
        PGAPI_FreeStmt(hstmt, SQL_DROP);
        return;
    }

    result = PGAPI_Fetch(hstmt);
    if (!SQL_SUCCEEDED(result))
    {
        PGAPI_FreeStmt(hstmt, SQL_DROP);
        return;
    }

    result = PGAPI_GetData(hstmt, 1, SQL_C_CHAR, self->pg_version, MAX_INFO_STRING, NULL);
    if (!SQL_SUCCEEDED(result))
    {
        PGAPI_FreeStmt(hstmt, SQL_DROP);
        return;
    }

    /*
     * Extract the Major and Minor numbers from the string. This assumes
     * the string starts 'Postgresql X.X'
     */
    strcpy(szVersion, "0.0");
    if (sscanf(self->pg_version, "%*s %d.%d", &major, &minor) >= 2)
    {
        snprintf(szVersion, sizeof(szVersion), "%d.%d", major, minor);
        self->pg_version_major = major;
        self->pg_version_minor = minor;
    }
    self->pg_version_number = (float) atof(szVersion);
    if (PG_VERSION_GE(self, 7.3))
        self->schema_support = 1;

    mylog("Got the PostgreSQL version string: '%s'\n", self->pg_version);
    mylog("Extracted PostgreSQL version number: '%1.1f'\n", self->pg_version_number);
    qlog("    [ PostgreSQL version string = '%s' ]\n", self->pg_version);
    qlog("    [ PostgreSQL version number = '%1.1f' ]\n", self->pg_version_number);

    result = PGAPI_FreeStmt(hstmt, SQL_DROP);
}


void
CC_log_error(const char *func, const char *desc, const ConnectionClass *self)
{
#ifdef PRN_NULLCHECK
#define nullcheck(a) (a ? a : "(NULL)")
#endif

    if (self)
    {
        qlog("CONN ERROR: func=%s, desc='%s', errnum=%d, errmsg='%s'\n", func, desc, self->__error_number, nullcheck(self->__error_message));
        mylog("CONN ERROR: func=%s, desc='%s', errnum=%d, errmsg='%s'\n", func, desc, self->__error_number, nullcheck(self->__error_message));
        qlog("            ------------------------------------------------------------\n");
        qlog("            henv=%p, conn=%p, status=%u, num_stmts=%d\n", self->henv, self, self->status, self->num_stmts);
        qlog("            sock=%p, stmts=%p, lobj_type=%d\n", self->sock, self->stmts, self->lobj_type);

        qlog("            ---------------- Socket Info -------------------------------\n");
        if (self->sock)
        {
            SocketClass *sock = self->sock;

            qlog("            socket=%d, reverse=%d, errornumber=%d, errormsg='%s'\n", sock->socket, sock->reverse, sock->errornumber, nullcheck(SOCK_get_errmsg(sock)));
            qlog("            buffer_in=%u, buffer_out=%u\n", sock->buffer_in, sock->buffer_out);
            qlog("            buffer_filled_in=%d, buffer_filled_out=%d, buffer_read_in=%d\n", sock->buffer_filled_in, sock->buffer_filled_out, sock->buffer_read_in);
        }
    }
    else
{
        qlog("INVALID CONNECTION HANDLE ERROR: func=%s, desc='%s'\n", func, desc);
        mylog("INVALID CONNECTION HANDLE ERROR: func=%s, desc='%s'\n", func, desc);
}
#undef PRN_NULLCHECK
}

int
CC_get_max_query_len(const ConnectionClass *conn)
{
    int         value;

    /* Long Queries in 7.0+ */
    if (PG_VERSION_GE(conn, 7.0))
        value = 0 /* MAX_STATEMENT_LEN */ ;
    /* Prior to 7.0 we used 2*BLCKSZ */
    else if (PG_VERSION_GE(conn, 6.5))
        value = (2 * BLCKSZ);
    else
        /* Prior to 6.5 we used BLCKSZ */
        value = BLCKSZ;
    return value;
}

/*
 *  This doesn't really return the CURRENT SCHEMA
 *  but there's no alternative.
 */
const char *
CC_get_current_schema(ConnectionClass *conn)
{
    if (!conn->current_schema && conn->schema_support)
    {
        QResultClass    *res;

        if (res = CC_send_query(conn, "select current_schema()", NULL, IGNORE_ABORT_ON_CONN | ROLLBACK_ON_ERROR, NULL), QR_command_maybe_successful(res))
        {
            if (QR_get_num_total_tuples(res) == 1)
                conn->current_schema = strdup(QR_get_value_backend_text(res, 0, 0));
        }
        QR_Destructor(res);
    }
    return (const char *) conn->current_schema;
}

static int LIBPQ_send_cancel_request(const ConnectionClass *conn);
int
CC_send_cancel_request(const ConnectionClass *conn)
{
    int         save_errno = SOCK_ERRNO;
    SOCKETFD        tmpsock = -1;
    struct
    {
        uint32      packetlen;
        CancelRequestPacket cp;
    }           crp;
    BOOL    ret = TRUE;
    SocketClass *sock;
    struct sockaddr *sadr;

    /* Check we have an open connection */
    if (!conn)
        return FALSE;
    sock = CC_get_socket(conn);
    if (!sock)
        return FALSE;

    /*
     * We need to open a temporary connection to the postmaster. Use the
     * information saved by connectDB to do this with only kernel calls.
    */
    sadr = (struct sockaddr *) &(sock->sadr_area);
    if ((tmpsock = socket(sadr->sa_family, SOCK_STREAM, 0)) < 0)
    {
        return FALSE;
    }
    if (connect(tmpsock, sadr, sock->sadr_len) < 0)
    {
        closesocket(tmpsock);
        return FALSE;
    }

    /*
     * We needn't set nonblocking I/O or NODELAY options here.
     */
    crp.packetlen = htonl((uint32) sizeof(crp));
    crp.cp.cancelRequestCode = (MsgType) htonl(CANCEL_REQUEST_CODE);
    crp.cp.backendPID = htonl(conn->be_pid);
    crp.cp.cancelAuthCode = htonl(conn->be_key);

    while (send(tmpsock, (char *) &crp, sizeof(crp), SEND_FLAG) != (int) sizeof(crp))
    {
        if (SOCK_ERRNO != EINTR)
        {
            save_errno = SOCK_ERRNO;
            ret = FALSE;
            break;
        }
    }
    if (ret)
    {
        while (recv(tmpsock, (char *) &crp, 1, RECV_FLAG) < 0)
        {
            if (EINTR != SOCK_ERRNO)
                break;
        }
    }

    /* Sent it, done */
    closesocket(tmpsock);
    SOCK_ERRNO_SET(save_errno);

    return ret;
}

int CC_mark_a_object_to_discard(ConnectionClass *conn, int type, const char *plan)
{
    int cnt = conn->num_discardp + 1;
    char    *pname;
    
    CC_REALLOC_return_with_error(conn->discardp, char *,
        (cnt * sizeof(char *)), conn, "Couldn't alloc discardp.", -1);
    CC_MALLOC_return_with_error(pname, char, (strlen(plan) + 2),
        conn, "Couldn't alloc discardp mem.", -1);
    pname[0] = (char) type; /* 's':prepared statement 'p':cursor */
    strcpy(pname + 1, plan);
    conn->discardp[conn->num_discardp++] = pname;

    return 1;
}

int CC_discard_marked_objects(ConnectionClass *conn)
{
    int i, cnt;
    QResultClass *res;
    char    *pname, cmd[64];

    if ((cnt = conn->num_discardp) <= 0)
        return 0;
    for (i = cnt - 1; i >= 0; i--)
    {
        pname = conn->discardp[i];
        if ('s' == pname[0])
                snprintf(cmd, sizeof(cmd), "DEALLOCATE \"%s\"", pname + 1);
        else
                snprintf(cmd, sizeof(cmd), "CLOSE \"%s\"", pname + 1);
        res = CC_send_query(conn, cmd, NULL, ROLLBACK_ON_ERROR | IGNORE_ABORT_ON_CONN, NULL);
        QR_Destructor(res);
        free(conn->discardp[i]);
        conn->num_discardp--;
    }

    return 1;
}

const char *CurrCat(const ConnectionClass *conn)
{
    /*
     * Returning the database name causes problems in MS Query. It
     * generates query like: "SELECT DISTINCT a FROM byronnbad3
     * bad3"
     */
    if (isMsQuery())    /* MS Query */
        return NULL;
    else if (conn->schema_support)
        return conn->connInfo.database;
    else
        return NULL;
}

const char *CurrCatString(const ConnectionClass *conn)
{
    const char *cat = CurrCat(conn);

    if (!cat)
        cat = NULL_STRING;
    return cat;
}
