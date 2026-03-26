#!/usr/bin/env python3
"""
Script to create a sample DOCX file for testing LaTeX export.
This creates a properly structured research paper document.
"""

from docx import Document
from docx.shared import Pt, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH

# Create a new Document
doc = Document()

# Add title
title = doc.add_heading('Machine Learning for Climate Prediction: A Comprehensive Review', level=0)
title.alignment = WD_ALIGN_PARAGRAPH.CENTER

# Add abstract section
abstract_heading = doc.add_heading('Abstract', level=1)
abstract_text = doc.add_paragraph(
    'Climate change poses one of the greatest challenges of our time. Machine learning models '
    'have demonstrated remarkable potential in predicting climate patterns and understanding complex '
    'atmospheric dynamics. This paper presents a comprehensive review of recent advances in applying '
    'machine learning techniques to climate prediction. We discuss various neural network architectures, '
    'including convolutional neural networks (CNNs), recurrent neural networks (RNNs), and transformer '
    'models, and their applications in forecasting temperature, precipitation, and extreme weather events. '
    'Furthermore, we examine challenges such as data scarcity, model interpretability, and computational '
    'requirements. Our findings suggest that ensemble methods combining multiple machine learning models '
    'yield the most promising results for long-term climate predictions.'
)

# Add keywords
keywords_heading = doc.add_heading('Keywords', level=1)
keywords = doc.add_paragraph('machine learning, climate prediction, neural networks, deep learning, weather forecasting')

# Add Introduction
intro_heading = doc.add_heading('Introduction', level=1)
intro_para1 = doc.add_paragraph(
    'Climate modeling has traditionally relied on complex physical simulations based on fundamental '
    'equations of fluid dynamics and thermodynamics. However, recent advances in machine learning have '
    'opened new avenues for climate prediction [1]. These data-driven approaches can capture complex '
    'patterns that traditional models may miss, particularly for extreme weather events [2].'
)
intro_para2 = doc.add_paragraph(
    'The primary motivation for applying machine learning to climate science is threefold: (1) improved '
    'computational efficiency, (2) ability to learn from high-dimensional observational data, and (3) '
    'potential for more accurate short-term forecasts [3]. Early studies showed that recurrent neural '
    'networks could match or exceed the performance of traditional weather prediction systems.'
)

# Add Related Work
related_heading = doc.add_heading('Related Work', level=1)
related_para1 = doc.add_paragraph(
    'The intersection of machine learning and climate science is not new. Smith et al. [4] pioneered '
    'the use of neural networks for weather prediction in the early 2000s. Their work demonstrated that '
    'simple feedforward networks could learn weather patterns from historical data.'
)
related_para2 = doc.add_paragraph(
    'More recently, deep learning approaches have dominated the field. The work by Johnson et al. [5] '
    'introduced convolutional neural networks specifically designed for spatial climate data. Their '
    'architecture could process global gridded data efficiently and produce meaningful climate predictions.'
)

# Add Methodology
method_heading = doc.add_heading('Methodology', level=1)
method_para = doc.add_paragraph(
    'Our review follows the PRISMA guidelines for systematic reviews. We searched PubMed, IEEE Xplore, '
    'and arXiv databases for papers published between 2018 and 2024. Search terms included combinations of '
    '"machine learning", "climate prediction", "neural networks", and "weather forecasting". We included '
    'peer-reviewed articles, conference proceedings, and preprints that directly addressed climate or weather '
    'prediction using machine learning techniques.'
)

# Add Results
results_heading = doc.add_heading('Results', level=1)
results_para1 = doc.add_paragraph(
    'Our search identified 127 relevant papers. After removing duplicates, we analyzed 89 studies. '
    'The majority (68%) used deep learning approaches, with CNNs and RNNs being the most common architectures [6].'
)
results_para2 = doc.add_paragraph(
    'Table 1 summarizes the key findings. Neural network-based models achieved mean absolute percentage '
    'errors (MAPE) ranging from 3.2% to 8.7% for temperature prediction [7, 8]. For precipitation forecasting, '
    'the results were more variable, with MAPEs between 12% and 35%, depending on the region and prediction horizon.'
)

# Add Discussion
discussion_heading = doc.add_heading('Discussion', level=1)
discussion_para1 = doc.add_paragraph(
    'The results clearly demonstrate that machine learning has made significant contributions to climate science. '
    'However, several challenges remain. First, the "black box" nature of deep learning models makes interpretation '
    'difficult, which is problematic for scientific applications where understanding the reasoning behind predictions '
    'is crucial [9].'
)
discussion_para2 = doc.add_paragraph(
    'Second, most studies are trained on relatively short time periods (5-30 years), which may not be sufficient to '
    'capture multi-decadal climate variability. Third, climate systems are non-stationary, meaning the statistical '
    'relationships learned during training may not hold during deployment [10].'
)

# Add Conclusion
conclusion_heading = doc.add_heading('Conclusion', level=1)
conclusion_para = doc.add_paragraph(
    'Machine learning offers promising opportunities for improving climate and weather predictions. The combination of '
    'large observational datasets and powerful neural network architectures has already yielded competitive results compared '
    'to traditional climate models. Future work should focus on (1) developing more interpretable models, (2) incorporating '
    'physics-informed constraints, and (3) improving long-term prediction capabilities.'
)

# Add References section
ref_heading = doc.add_heading('References', level=1)
references = [
    '[1] Author, A.: Deep learning for weather prediction. Nature, 2022.',
    '[2] Author, B., Author, C.: Machine learning in climate science. Journal of Climate, 35(5), 1234-1250, 2023.',
    '[3] Author, D.: Computational efficiency of neural networks. IEEE Transactions, 45(2), 567-580, 2022.',
    '[4] Smith, J., Jones, K.: Neural networks for forecasting. Weather Review, 130(8), 1900-1920, 2002.',
    '[5] Johnson, R., Brown, L., Green, M.: CNNs for climate data. Geoscience Letters, 8(3), 15, 2021.',
    '[6] Editor, E., Editor, F.: Survey of deep learning in weather. Surveys in Geophysics, 42(4), 865-902, 2021.',
    '[7] Author, G.: Temperature prediction accuracy. Climate Dynamics, 56(1), 123-145, 2020.',
    '[8] Author, H., Author, I.: Improving neural network forecasts. Monthly Weather Review, 148(9), 3567-3585, 2020.',
    '[9] Author, J.: Interpretability of deep learning. IEEE Access, 9, 12345-12360, 2021.',
    '[10] Author, K., Author, L.: Non-stationary climate systems. Climate Research, 81(2), 135-152, 2023.',
]

for ref in references:
    doc.add_paragraph(ref)

# Save the document
output_path = '/home/purva/Documents/College/Semester6/OOAD/MiniProject/Draftly/sample_paper.docx'
doc.save(output_path)
print(f'✅ Sample DOCX file created: {output_path}')
