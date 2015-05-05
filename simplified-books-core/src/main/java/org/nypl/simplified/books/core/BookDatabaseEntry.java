package org.nypl.simplified.books.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.nypl.simplified.opds.core.OPDSAcquisitionFeedEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;

/**
 * A single book directory.
 *
 * All operations on the directory are serialized by acquiring a mandatory
 * lock on a <tt>lock</tt> file inside the directory for the duration of the
 * changes. Due to limitations in the Android and Java APIs, threads must
 * busy-wait on acquiring file locks. Threads contending for the locks will
 * try for at most {@link #WAIT_MAXIMUM_MILLISECONDS} milliseconds, pausing
 * for {@link #WAIT_PAUSE_MILLISECONDS} between lock attempts before failing.
 */

@SuppressWarnings("synthetic-access") public final class BookDatabaseEntry implements
  BookDatabaseEntryType
{
  private static final long serialVersionUID          = 1L;
  public static final int   WAIT_MAXIMUM_MILLISECONDS = 1000;
  public static final int   WAIT_PAUSE_MILLISECONDS   = 10;

  private final File        directory;
  private final File        file_book;
  private final File        file_cover;
  private final File        file_download_id;
  private final File        file_download_id_tmp;
  private final File        file_lock;
  private final File        file_meta;
  private final File        file_meta_tmp;
  private final BookID      id;

  public BookDatabaseEntry(
    final File parent,
    final BookID book_id)
  {
    this.directory =
      new File(NullCheck.notNull(parent), NullCheck
        .notNull(book_id)
        .toString());

    this.id = NullCheck.notNull(book_id);
    this.file_lock = new File(this.directory, "lock");
    this.file_cover = new File(this.directory, "cover.jpg");
    this.file_meta = new File(this.directory, "meta.dat");
    this.file_meta_tmp = new File(this.directory, "meta.dat.tmp");
    this.file_book = new File(this.directory, "book.epub");

    this.file_download_id = new File(this.directory, "download_id.txt");
    this.file_download_id_tmp =
      new File(this.directory, "download_id.txt.tmp");
  }

  @Override public void copyInBookFromSameFilesystem(
    final File file)
    throws IOException
  {
    FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, Unit, IOException>() {
        @Override public Unit call(
          final Unit x)
          throws IOException
        {
          BookDatabaseEntry.this.copyInBookFromSameFilesystemLocked(file);
          return Unit.unit();
        }
      });
  }

  private void copyInBookFromSameFilesystemLocked(
    final File file)
    throws IOException
  {
    FileUtilities.fileRename(file, this.file_book);

    this.file_download_id.delete();
    if (this.file_download_id.exists()) {
      throw new IOException(String.format(
        "Could not delete '%s'",
        this.file_download_id));
    }
  }

  @Override public void create()
    throws IOException
  {
    FileUtilities.createDirectory(this.directory);
  }

  @Override public void destroy()
    throws IOException
  {
    FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, Unit, IOException>() {
        @Override public Unit call(
          final Unit x)
          throws IOException
        {
          BookDatabaseEntry.this.destroyLocked();
          return Unit.unit();
        }
      });
  }

  private void destroyLocked()
    throws IOException
  {
    if (this.directory.isDirectory()) {
      final TreeTraverser<File> trav = Files.fileTreeTraverser();
      final ImmutableList<File> list =
        trav.postOrderTraversal(this.directory).toList();

      for (int index = 0; index < list.size(); ++index) {
        final File file = list.get(index);
        final boolean ok = file.delete();
        if (ok == false) {
          throw new IOException("Unable to delete: " + file);
        }
      }
    }
  }

  @Override public boolean exists()
  {
    return this.file_meta.isFile();
  }

  private OptionType<File> getBookLocked()
  {
    if (this.file_book.isFile()) {
      return Option.some(this.file_book);
    }
    return Option.none();
  }

  @Override public OptionType<File> getCover()
    throws IOException
  {
    return FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, OptionType<File>, IOException>() {
        @Override public OptionType<File> call(
          final Unit x)
          throws IOException
        {
          return BookDatabaseEntry.this.getCoverLocked();
        }
      });
  }

  private OptionType<File> getCoverLocked()
  {
    if (this.file_cover.isFile()) {
      return Option.some(this.file_cover);
    }
    return Option.none();
  }

  @Override public OPDSAcquisitionFeedEntry getData()
    throws IOException
  {
    return FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, OPDSAcquisitionFeedEntry, IOException>() {
        @Override public OPDSAcquisitionFeedEntry call(
          final Unit x)
          throws IOException
        {
          return BookDatabaseEntry.this.getDataLocked();
        }
      });
  }

  private OPDSAcquisitionFeedEntry getDataLocked()
    throws IOException
  {
    final ObjectInputStream is =
      new ObjectInputStream(new FileInputStream(this.file_meta));
    try {
      return NullCheck.notNull((OPDSAcquisitionFeedEntry) is.readObject());
    } catch (final ClassNotFoundException e) {
      throw new IOException(e);
    } finally {
      is.close();
    }
  }

  @Override public File getDirectory()
  {
    return this.directory;
  }

  @Override public OptionType<Long> getDownloadID()
    throws IOException
  {
    return FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, OptionType<Long>, IOException>() {
        @Override public OptionType<Long> call(
          final Unit x)
          throws IOException
        {
          return BookDatabaseEntry.this.getDownloadIDLocked();
        }
      });
  }

  private OptionType<Long> getDownloadIDLocked()
    throws IOException
  {
    if (this.file_download_id.isFile()) {
      final Long i =
        Long.valueOf(FileUtilities.fileReadUTF8(this.file_download_id));
      return Option.some(NullCheck.notNull(i));
    }
    return Option.none();
  }

  private OPDSAcquisitionFeedEntry getEntryLocked()
    throws IOException
  {
    final ObjectInputStream is =
      new ObjectInputStream(new FileInputStream(this.file_meta));
    try {
      final OPDSAcquisitionFeedEntry e =
        (OPDSAcquisitionFeedEntry) is.readObject();
      return NullCheck.notNull(e);
    } catch (final ClassNotFoundException x) {
      throw new IOException(x);
    } finally {
      is.close();
    }
  }

  @Override public BookID getID()
  {
    return this.id;
  }

  @Override public BookSnapshot getSnapshot()
    throws IOException
  {
    return FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, BookSnapshot, IOException>() {
        @Override public BookSnapshot call(
          final Unit x)
          throws IOException
        {
          return BookDatabaseEntry.this.getSnapshotLocked();
        }
      });
  }

  private BookSnapshot getSnapshotLocked()
    throws IOException
  {
    final OPDSAcquisitionFeedEntry in_entry = this.getEntryLocked();
    final OptionType<Long> in_download_id = this.getDownloadIDLocked();
    final OptionType<File> in_cover = this.getCoverLocked();
    final OptionType<File> in_book = this.getBookLocked();
    return new BookSnapshot(in_cover, in_book, in_download_id, in_entry);
  }

  @Override public void setData(
    final OptionType<File> in_cover,
    final OPDSAcquisitionFeedEntry in_entry)
    throws IOException
  {
    FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, Unit, IOException>() {
        @Override public Unit call(
          final Unit x)
          throws IOException
        {
          BookDatabaseEntry.this.setDataLocked(in_cover, in_entry);
          return Unit.unit();
        }
      });
  }

  private void setDataLocked(
    final OptionType<File> in_cover,
    final OPDSAcquisitionFeedEntry in_entry)
    throws IOException
  {
    if (in_cover.isSome()) {
      final Some<File> some = (Some<File>) in_cover;
      FileUtilities.fileCopy(some.get(), this.file_cover);
      some.get().delete();
    } else {
      this.file_cover.delete();
    }

    {
      final ObjectOutputStream os =
        new ObjectOutputStream(new FileOutputStream(this.file_meta_tmp));
      try {
        os.writeObject(in_entry);
        os.flush();
      } finally {
        os.close();
      }

      FileUtilities.fileRename(this.file_meta_tmp, this.file_meta);
    }
  }

  @Override public void setDownloadID(
    final long did)
    throws IOException
  {
    FileLocking.withFileLocked(
      this.file_lock,
      BookDatabaseEntry.WAIT_PAUSE_MILLISECONDS,
      BookDatabaseEntry.WAIT_MAXIMUM_MILLISECONDS,
      new PartialFunctionType<Unit, Unit, IOException>() {
        @Override public Unit call(
          final Unit x)
          throws IOException
        {
          BookDatabaseEntry.this.setDownloadIDLocked(did);
          return Unit.unit();
        }
      });
  }

  private void setDownloadIDLocked(
    final long did)
    throws IOException
  {
    FileUtilities.fileWriteUTF8Atomically(
      this.file_download_id,
      this.file_download_id_tmp,
      NullCheck.notNull(Long.toString(did)));
  }
}