package de.cronn.validation_files_diff;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBTextField;
import de.cronn.validation_files_diff.helper.DiffSide;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class ValidationDiffSettingsConfigurableGUI {
	private final ValidationDiffApplicationOptionsProvider applicationSettings;
	private final ValidationDiffProjectOptionsProvider projectSettings;
	private JPanel projectSettingsPanel;
	private JPanel globalSettingsPanel;
	private JTextField outputDirPath;
	private JTextField validationDirPath;
	private JComboBox<DiffSide> validationSide;
	private JComboBox<DiffSide> outputSide;
	private JPanel rootPanel;

	private JCheckBox showNewSrc;
	private JLabel showNewSrcIcon;
	private JCheckBox showDifference;
	private JLabel showDifferenceIcon;
	private JCheckBox showEqual;
	private JLabel showEqualIcon;
	private JCheckBox showNewTarget;
	private JLabel showNewTargetIcon;
	private JLabel showNewSrcText;
	private JLabel showDiffText;
	private JLabel showEqualText;
	private JLabel showNewTargetText;


	ValidationDiffSettingsConfigurableGUI(ValidationDiffApplicationOptionsProvider applicationSettings, ValidationDiffProjectOptionsProvider projectSettings) {
		this.applicationSettings = applicationSettings;
		this.projectSettings = projectSettings;

		projectSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder("Project settings"));
		globalSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder("Application settings"));
	}

	private static void setupTextFieldDefaultValue(@NotNull JTextField textField, @NotNull Supplier<String> defaultValueSupplier) {
		String defaultPath = defaultValueSupplier.get();
		if (StringUtil.isEmptyOrSpaces(defaultPath)) return;
		textField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				textField.setForeground(defaultPath.equals(textField.getText()) ? getDefaultValueColor() : getChangedValueColor());
			}
		});
		if (textField instanceof JBTextField) {
			((JBTextField) textField).getEmptyText().setText(defaultPath);
		}
	}

	private static Color getChangedValueColor() {
		return findColorByKey("TextField.foreground");
	}

	private static Color getDefaultValueColor() {
		return findColorByKey("TextField.inactiveForeground", "nimbusDisabledText");
	}

	@NotNull
	private static Color findColorByKey(String... colorKeys) {
		return Arrays.stream(colorKeys)
				.map(UIManager::getColor)
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Can't find color for keys " + Arrays.toString(colorKeys)));
	}

	private static <T> T getSelectedItemFromJComboBox(JComboBox<T> jComboBox) {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex());
	}

	JComponent createPanel() {
		setupIcons();
		setupValues();
		setupListeners();

		return rootPanel;
	}

	private void setupIcons() {
		showNewSrcIcon.setIcon(AllIcons.Vcs.Arrow_right);
		showDifferenceIcon.setIcon(AllIcons.Vcs.Not_equal);
		showEqualIcon.setIcon(AllIcons.Vcs.Equal);
		showNewTargetIcon.setIcon(AllIcons.Vcs.Arrow_left);
	}

	private void setupListeners() {
		setupTextFieldDefaultValue(outputDirPath, () -> ValidationDiffProjectOptionsProvider.DEFAULT_OUTPUT_DIRECTORY);
		setupTextFieldDefaultValue(validationDirPath, () -> ValidationDiffProjectOptionsProvider.DEFAULT_VALIDATION_DIRECTORY);

		validationSide.addActionListener(togglingActionListener(validationSide, outputSide));
		outputSide.addActionListener(togglingActionListener(outputSide, validationSide));

		setupJLabelToggleCheckbox(showEqual, showEqualIcon, showEqualText);
		setupJLabelToggleCheckbox(showDifference, showDifferenceIcon, showDiffText);
		setupJLabelToggleCheckbox(showNewSrc, showNewSrcIcon, showNewSrcText);
		setupJLabelToggleCheckbox(showNewTarget, showNewTargetIcon, showNewTargetText);
	}

	@NotNull
	private ActionListener togglingActionListener(JComboBox<DiffSide> changedElement, JComboBox<DiffSide> elementToChange) {
		return actionEvent -> elementToChange.setSelectedIndex(changedElement.getSelectedIndex() == 0 ? 1 : 0);
	}

	private void setupJLabelToggleCheckbox(JCheckBox jCheckBox, JLabel... jLabels) {
		for (JLabel jLabel : jLabels) {
			jLabel.addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					jCheckBox.setSelected(!jCheckBox.isSelected());
				}
			});
		}
	}

	private void setupValues() {
		validationSide.addItem(DiffSide.LEFT);
		validationSide.addItem(DiffSide.RIGHT);

		outputSide.addItem(DiffSide.LEFT);
		outputSide.addItem(DiffSide.RIGHT);
	}

	void apply() {
		final DiffSide outputSideSelectedItem = getSelectedItemFromJComboBox(outputSide);
		if (applicationSettings != null) {
			if (applicationSettings.getShowNewOnSource() != showNewSrc.isSelected()) {
				applicationSettings.setShowNewOnSource(showNewSrc.isSelected());
			}

			if (applicationSettings.getShowDifferent() != showDifference.isSelected()) {
				applicationSettings.setShowDifferent(showDifference.isSelected());
			}

			if (applicationSettings.getShowEqual() != showEqual.isSelected()) {
				applicationSettings.setShowEqual(showEqual.isSelected());
			}

			if (applicationSettings.getShowNewOnTarget() != showNewTarget.isSelected()) {
				applicationSettings.setShowNewOnTarget(showNewTarget.isSelected());
			}

			if (!applicationSettings.getOutputSide().equals(outputSideSelectedItem)) {
				applicationSettings.setOutputSide(outputSideSelectedItem);
			}
		}
		if (projectSettings != null) {
			final String outputDirPathFromTextField = outputDirPath.getText();
			if (!projectSettings.getRelativeOutputDirPath().equals(outputDirPathFromTextField)) {
				projectSettings.setRelativeOutputDirPath(outputDirPathFromTextField);
			}
			final String validationDirPathFromTextField = validationDirPath.getText();
			if (!projectSettings.getRelativeValidationDirPath().equals(validationDirPathFromTextField)) {
				projectSettings.setRelativeValidationDirPath(validationDirPathFromTextField);
			}
		}

	}

	boolean isModified() {
		if (applicationSettings == null || projectSettings == null) {
			return false;
		}
		return !(projectSettings.getRelativeOutputDirPath().equals(outputDirPath.getText())
				&& projectSettings.getRelativeValidationDirPath().equals(validationDirPath.getText())
				&& applicationSettings.getOutputSide().equals(getSelectedItemFromJComboBox(outputSide))
				&& applicationSettings.getShowNewOnSource() == showNewSrc.isSelected()
				&& applicationSettings.getShowDifferent() == showDifference.isSelected()
				&& applicationSettings.getShowEqual() == showEqual.isSelected()
				&& applicationSettings.getShowNewOnTarget() == showNewTarget.isSelected());

	}

	void reset() {
		if (projectSettings != null) {
			validationDirPath.setText(projectSettings.getRelativeValidationDirPath());
			outputDirPath.setText(projectSettings.getRelativeOutputDirPath());
		}
		if (applicationSettings != null) {
			showNewSrc.setSelected(applicationSettings.getShowNewOnSource());
			showDifference.setSelected(applicationSettings.getShowDifferent());
			showEqual.setSelected(applicationSettings.getShowEqual());
			showNewTarget.setSelected(applicationSettings.getShowNewOnTarget());

			switch (applicationSettings.getOutputSide()) {
				case LEFT:
					outputSide.setSelectedItem(DiffSide.LEFT);
					validationSide.setSelectedItem(DiffSide.RIGHT);
					break;
				case RIGHT:
					outputSide.setSelectedItem(DiffSide.RIGHT);
					validationSide.setSelectedItem(DiffSide.LEFT);
					break;
				default:
					break;
			}
		}
	}
}
