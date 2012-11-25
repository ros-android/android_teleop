/*
 * Copyright (c) 2011, Willow Garage, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Willow Garage, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ros.android.teleop;

import org.ros.exception.RosException;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;

import ros.android.activity.RosAppActivity;
import ros.android.views.JoystickView;
import ros.android.views.SensorImageView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
//import ros.android.activity.R;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class Teleop extends RosAppActivity {
  private SensorImageView cameraView;
  private String robotAppName;
  private String cameraTopic;
  private JoystickView joystickView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    setDefaultAppName("turtlebot_teleop/android_teleop");
    setDashboardResource(R.id.top_bar);
    setMainWindowResource(R.layout.main);
    super.onCreate(savedInstanceState);

    joystickView = (JoystickView)findViewById(R.id.joystick);
    if (getIntent().hasExtra("base_control_topic")) {
      joystickView.setBaseControlTopic(getIntent().getStringExtra("base_control_topic"));
    }
    if (getIntent().hasExtra("camera_topic")) {
      cameraTopic = getIntent().getStringExtra("camera_topic");
    } else {
      cameraTopic = "camera/rgb/image_color/compressed_throttle";
    }

    cameraView = (SensorImageView) findViewById(R.id.image);
  }

  @Override
  protected void onNodeDestroy(Node node) {
    if (cameraView != null) {
      cameraView.stop();
      cameraView = null;
    }
    if (joystickView != null) {
      joystickView.stop();
      joystickView = null;
    }
    super.onNodeDestroy(node);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Toast.makeText(Teleop.this, "starting app", Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onNodeCreate(ConnectedNode node) {
    Log.i("Teleop", "startAppFuture");
    super.onNodeCreate(node);
    try {
      NameResolver appNamespace = getAppNamespace(node);
      cameraView = (SensorImageView) findViewById(R.id.image);
      Log.i("Teleop", "init cameraView");
      cameraView.start(node, appNamespace.resolve(cameraTopic).toString());
      cameraView.post(new Runnable() {
        @Override
        public void run() {
          cameraView.setSelected(true);
        }});
      joystickView.start(node);
    } catch (RosException ex) {
      Toast.makeText(Teleop.this, "Failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.teleop_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.kill:
      android.os.Process.killProcess(android.os.Process.myPid());
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

}
