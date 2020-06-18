# GGLauncher

![Screenshots](https://user-images.githubusercontent.com/5319256/84963889-9b06df00-b145-11ea-93cc-181b5287b0df.png)

GGLauncher is a kiosk application for [Google Glass Enterprise Edition 2](https://www.google.com/glass/start/).

## Features
It has the following features

- Clock and news feed display
- Access to camera and gallery apps
- Using Google Assistant
- Video playback from YouTube
- Waking Up by Face Angle
- power-saving mode

## Installation

- To install it, you need to get access to `Google Assistant SDK` and the `Youtube Data API` by using [google-oauthlib-tool](https://developers.google.com/assistant/sdk/guides/library/python/embed/install-sample) in advance.
  Please save credential file as `PRODJECT_DIR/app/src/main/res/raw/credentials.json`.
  Here's the scope you need:
  - https://www.googleapis.com/auth/assistant-sdk-prototype
  - https://www.googleapis.com/auth/youtube

- Also, you need to put the Model ID in `src/main/res /raw/google_assistant_sdk_device_model_id.txt` obtained from the Actions Console.


## Future Plans

The following features are planned to be implemented in the future

- Most recent schedule
- View notifications on paired iOS devices
- App launcher function for debugging
- View the weather forecast
- Volume Switching Function
