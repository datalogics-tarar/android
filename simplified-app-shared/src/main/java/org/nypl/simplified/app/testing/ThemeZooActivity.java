package org.nypl.simplified.app.testing;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import org.nypl.simplified.app.R;
import org.nypl.simplified.app.utilities.UIThread;

/**
 * A secret activity showing off the UI elements and color scheme.
 */

public final class ThemeZooActivity extends Activity
{
  /**
   * Construct an activity.
   */

  public ThemeZooActivity()
  {

  }

  @Override protected void onCreate(
    final @Nullable Bundle state)
  {
    super.onCreate(state);
    this.setContentView(R.layout.theme_zoo);

    final ProgressBar pro_0 = NullCheck.notNull(
      (ProgressBar) this.findViewById(R.id.theme_zoo_downloading_progress));
    final ProgressBar pro_1 = NullCheck.notNull(
      (ProgressBar) this.findViewById(R.id.theme_zoo_normal_progress));

    final Runnable r = new Runnable()
    {
      @Override public void run()
      {
        pro_0.setProgress((pro_0.getProgress() + 10) % 100);
        pro_1.setProgress((pro_1.getProgress() + 10) % 100);
        UIThread.runOnUIThreadDelayed(this, 1000L);
      }
    };

    UIThread.runOnUIThreadDelayed(r, 1000L);
  }
}
