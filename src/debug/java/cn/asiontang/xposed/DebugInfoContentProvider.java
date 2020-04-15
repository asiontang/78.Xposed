package cn.asiontang.xposed;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.Collections;

/**
 * Android 四大组件之 ContentProvider - 掘金		https://juejin.im/post/5cab6ac35188251afa548943
 */
public class DebugInfoContentProvider extends android.content.ContentProvider
{
    /** The authority for the contacts provider */
    public static final String AUTHORITY = "DebugInfoContentProvider";
    /** A content:// style uri to the authority for the contacts provider */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,
            "COLUMN_NAME_NEW_APK_FULL_PATH");
    public static final String COLUMN_NAME_NEW_APK_FULL_PATH = "NEW_APK_FULL_PATH";

    @Override
    public boolean onCreate()
    {
        return false;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
    {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                COLUMN_NAME_NEW_APK_FULL_PATH
        }, 1);
        matrixCursor.addRow(Collections.singletonList(DebugModeUtils.getApkFileFullPath()));
        return matrixCursor;
    }

    @Override
    public String getType(final Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
    {
        return 0;
    }
}
