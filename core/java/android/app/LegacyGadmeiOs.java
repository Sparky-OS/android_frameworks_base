// SPDX-License-Identifier: Apache-2.0
package android.app;

import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.SparseArray;
import android.view.LegacyGadmeiClient;

import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import libcore.io.ForwardingOs;
import libcore.io.Os;

/** Intercept only the family's three Java file endpoints, never host sysfs. */
final class LegacyGadmeiOs extends ForwardingOs {
    private final SparseArray<String> mControls = new SparseArray<>();

    private LegacyGadmeiOs(Os os) { super(os); }

    static void install() {
        if (!LegacyGadmeiClient.enabled()) return;
        Os previous;
        do {
            previous = Os.getDefault();
        } while (!Os.compareAndSetDefault(previous, new LegacyGadmeiOs(previous)));
    }

    @Override
    public boolean access(String path, int mode) throws ErrnoException {
        if (!LegacyGadmeiClient.isControl(path)) return super.access(path, mode);
        return (mode & OsConstants.X_OK) == 0;
    }

    @Override
    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
        if (!LegacyGadmeiClient.isControl(path)) return super.open(path, flags, mode);
        FileDescriptor fd = super.open("/dev/null",
                (flags & OsConstants.O_ACCMODE) | OsConstants.O_CLOEXEC, 0);
        synchronized (mControls) { mControls.put(fd.getInt$(), path); }
        return fd;
    }

    @Override
    public void close(FileDescriptor fd) throws ErrnoException {
        synchronized (mControls) { mControls.remove(fd.getInt$()); }
        super.close(fd);
    }

    private String control(FileDescriptor fd) {
        synchronized (mControls) { return mControls.get(fd.getInt$()); }
    }

    @Override
    public int write(FileDescriptor fd, byte[] bytes, int offset, int count)
            throws ErrnoException, InterruptedIOException {
        String path = control(fd);
        if (path == null) return super.write(fd, bytes, offset, count);
        if (offset < 0 || count < 0 || offset > bytes.length - count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (!fd.valid()) throw new ErrnoException("Gadmei closed control", OsConstants.EBADF);
        if (count == 0) return 0;
        if (count > 64) {
            android.util.Log.e("LegacyStereo", "Gadmei family write refused: oversized command");
            throw new ErrnoException("Gadmei control too long", OsConstants.EINVAL);
        }
        if (!LegacyGadmeiClient.write(path,
                new String(bytes, offset, count, StandardCharsets.US_ASCII))) {
            throw new ErrnoException("Gadmei control refused", OsConstants.EINVAL);
        }
        return count;
    }

    @Override
    public int write(FileDescriptor fd, ByteBuffer buffer)
            throws ErrnoException, InterruptedIOException {
        if (control(fd) == null) return super.write(fd, buffer);
        if (buffer.remaining() > 64) {
            throw new ErrnoException("Gadmei control too long", OsConstants.EINVAL);
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        int count = write(fd, bytes, 0, bytes.length);
        buffer.position(buffer.position() + count);
        return count;
    }
}
