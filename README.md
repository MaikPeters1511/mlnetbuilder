# ML.NET Builder for JetBrains Rider

![ML.NET Builder](src/main/resources/icons/mlnet-logo.svg)

**ML.NET Builder** brings the familiar Visual Studio Model Builder experience directly into JetBrains Rider. It allows you to build, train, and deploy custom machine learning models using ML.NET without leaving your favorite IDE.

## Features

- **Step-by-Step Wizard:** A guided UI workflow for seamless machine learning model creation.
- **AutoML Integration:** Automatically explores different algorithms and hyperparameters to find the best model for your data.
- **C# Code Generation:** Automatically generates ready-to-use consumption and training code for your new model.
- **Multiple Scenarios Supported:** 
  - Data Classification
  - Value Prediction (Regression)
  - Recommendation
  - Image Classification
  - Forecasting
  - Anomaly Detection
- **Visual Studio Compatibility:** Fully supports and manages `.mbconfig` files, ensuring compatibility with projects created using the Visual Studio ML.NET Model Builder.

## Prerequisites

- **JetBrains Rider** (Version 2024.1 or newer / Compatible with 2026.1)
- **.NET SDK** installed on your system.
- **ML.NET CLI:** The plugin relies on the `mlnet` global tool. You can install it via your terminal:
  ```bash
  dotnet tool install -g mlnet
  ```

## Installation

1. Download or build the latest release ZIP file (e.g., `ML.NET Builder-1.0.3.zip`).
2. Open JetBrains Rider.
3. Go to **Settings / Preferences** -> **Plugins**.
4. Click the gear icon ⚙️ at the top and select **Install Plugin from Disk...**
5. Select the downloaded ZIP file and restart Rider.

## Usage

There are three ways to launch the ML.NET Builder in Rider:

1. **Via the Tool Window:**
   - Open the **ML.NET Builder** tool window located at the bottom of the IDE.
   
2. **Via the Project View:**
   - Right-click anywhere in your project explorer.
   - Select **New** -> **Machine Learning Model (ML.NET)**.

3. **Via an existing `.mbconfig` file:**
   - Right-click any `.mbconfig` file in the project.
   - Select **Open in ML.NET Builder**.

## Configuration

You can configure the default behavior of the plugin by going to **Settings / Preferences** -> **Tools** -> **ML.NET Builder**.
Here you can specify:
- Default training time.
- GPU usage preference.
- Custom path to the `mlnet` CLI executable (if it is not found automatically).

## Development and Building

To build the plugin yourself from source, run the following Gradle task:

```bash
./gradlew buildPlugin
```

The compiled plugin will be located in the `build/distributions/` directory.

## License

This project is open-source and available under the MIT License.
