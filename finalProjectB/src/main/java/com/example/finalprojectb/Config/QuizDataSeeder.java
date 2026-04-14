package com.example.finalprojectb.Config;

import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.model.Question;
import com.example.finalprojectb.model.QuestionOption;
import com.example.finalprojectb.model.Quiz;
import com.example.finalprojectb.repo.ModuleRepository;
import com.example.finalprojectb.repo.QuestionOptionRepository;
import com.example.finalprojectb.repo.QuestionRepository;
import com.example.finalprojectb.repo.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(2)
public class QuizDataSeeder implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(QuizDataSeeder.class);

        @Autowired
        private ModuleRepository moduleRepo;
        @Autowired
        private QuizRepository quizRepo;
        @Autowired
        private QuestionRepository questionRepo;
        @Autowired
        private QuestionOptionRepository optionRepo;

        @Override
        @Transactional
        public void run(String... args) {
                seedQuiz("Installing Python and Pandas", "Installing Python and Pandas Quiz",
                                installingPythonAndPandasQuestions());
                seedQuiz("Data Cleaning", "Data Cleaning Quiz", dataCleansingQuestions());
                seedQuiz("Filtering and Grouping", "Filtering and Grouping Quiz", filteringAndGroupingQuestions());
                seedQuiz("Visualisation with Pandas", "Visualisation with Pandas Quiz",
                                visualisationWithPandasQuestions());
                seedQuiz("Foundations of Management", "Foundations of Management Quiz",
                                foundationsOfManagementQuestions());
                seedQuiz("Leadership and Team Dynamics", "Leadership and Team Dynamics Quiz",
                                leadershipAndTeamDynamicsQuestions());
                seedQuiz("Strategy and Decision Making", "Strategy and Decision Making Quiz",
                                strategyAndDecisionMakingQuestions());
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Core seeding logic
        // ─────────────────────────────────────────────────────────────────────────

        private void seedQuiz(String moduleTitle, String quizTitle, List<QD> questions) {
                List<Module> allModules = moduleRepo.findAll();

                // Log all available module titles on first call to help debug mismatches
                if (allModules.stream().noneMatch(m -> m.getTitle().trim().equalsIgnoreCase(moduleTitle.trim()))) {
                        logger.warn("QuizDataSeeder: module '{}' not found. Available modules in DB: {}",
                                        moduleTitle,
                                        allModules.stream().map(m -> "'" + m.getTitle() + "'").toList());
                        return;
                }

                Module module = allModules.stream()
                                .filter(m -> m.getTitle().trim().equalsIgnoreCase(moduleTitle.trim()))
                                .findFirst().orElse(null);

                if (module == null) {
                        logger.warn("QuizDataSeeder: module '{}' not found — skipping.", moduleTitle);
                        return;
                }

                boolean exists = !quizRepo.findByModuleIdOrderByOrderIndexAsc(module.getId()).isEmpty();
                if (exists) {
                        logger.info("QuizDataSeeder: quiz for '{}' already exists — skipping.", moduleTitle);
                        return;
                }

                Quiz quiz = new Quiz();
                quiz.setModule(module);
                quiz.setOrderIndex(1);
                quiz.setTitle(quizTitle);
                quiz.setPassingScore(70);
                quizRepo.save(quiz);

                for (int i = 0; i < questions.size(); i++) {
                        QD qd = questions.get(i);
                        Question q = new Question();
                        q.setQuiz(quiz);
                        q.setQuestionText(qd.text);
                        q.setQuestionType("MULTIPLE_CHOICE");
                        q.setDifficultyLevel(qd.difficulty);
                        q.setOrderIndex(i + 1);
                        q.setPoints(1);
                        q.setExplanation(qd.explanation);
                        questionRepo.save(q);

                        for (int j = 0; j < qd.options.length; j++) {
                                QuestionOption opt = new QuestionOption();
                                opt.setQuestion(q);
                                opt.setOptionText(qd.options[j]);
                                opt.setIsCorrect(j == 0); // index 0 (option A) is ALWAYS correct
                                opt.setOrderIndex(j + 1);
                                optionRepo.save(opt);
                        }
                }
                logger.info("QuizDataSeeder: seeded '{}' with {} questions.", quizTitle, questions.size());
        }

        record QD(String text, String[] options, String difficulty, String explanation) {
        }

        // =========================================================================
        // MODULE 1: Installing Python and Pandas
        // =========================================================================

        private List<QD> installingPythonAndPandasQuestions() {
                return List.of(
                                new QD("What is Python primarily used for in data analysis?",
                                                new String[] { "Processing and analyzing data", "Designing hardware",
                                                                "Managing networks", "Creating operating systems" },
                                                "EASY",
                                                "Python is widely used for processing, analyzing, and visualizing data."),

                                new QD("Which command installs pandas using pip?",
                                                new String[] { "pip install pandas", "install pandas pip",
                                                                "pip pandas install", "python pandas install" },
                                                "EASY",
                                                "The correct pip command is 'pip install pandas'."),

                                new QD("What does pip stand for?",
                                                new String[] { "Preferred Installer Program",
                                                                "Python Integration Package",
                                                                "Program Installation Protocol",
                                                                "Package Installer Process" },
                                                "EASY",
                                                "pip stands for Preferred Installer Program."),

                                new QD("Which file extension is used for Python files?",
                                                new String[] { ".py", ".java", ".txt", ".exe" },
                                                "EASY",
                                                "Python files use the .py extension."),

                                new QD("Which command checks Python version?",
                                                new String[] { "python --version", "check python", "python check",
                                                                "version python" },
                                                "EASY",
                                                "python --version shows the installed version."),

                                new QD("What is pandas mainly used for?",
                                                new String[] { "Data manipulation and analysis", "Game development",
                                                                "Web hosting", "Operating systems" },
                                                "EASY",
                                                "Pandas is used for handling structured data."),

                                new QD("Which data structure is core to pandas?",
                                                new String[] { "DataFrame", "ArrayList", "Stack", "Queue" },
                                                "EASY",
                                                "DataFrame is the main structure in pandas."),

                                new QD("How do you import pandas?",
                                                new String[] { "import pandas as pd", "import pd pandas",
                                                                "pandas import", "pd import pandas" },
                                                "EASY",
                                                "Standard practice is import pandas as pd."),

                                new QD("What does pd stand for?",
                                                new String[] { "Alias for pandas", "Python data", "Process data",
                                                                "Program dataset" },
                                                "EASY",
                                                "pd is just a shorthand alias."),

                                new QD("Which tool can run Python notebooks?",
                                                new String[] { "Jupyter Notebook", "Photoshop", "Excel", "Word" },
                                                "EASY",
                                                "Jupyter Notebook is commonly used for Python."),

                                new QD("What is Anaconda?",
                                                new String[] { "A Python distribution for data science",
                                                                "A programming language", "A database", "A browser" },
                                                "EASY",
                                                "Anaconda includes Python and many data science tools."),

                                new QD("Which command installs packages globally?",
                                                new String[] { "pip install package", "pip global install",
                                                                "install global", "python install global" },
                                                "EASY",
                                                "pip install installs packages globally by default."),

                                new QD("What does IDE stand for?",
                                                new String[] { "Integrated Development Environment",
                                                                "Internal Data Engine", "Interactive Design Element",
                                                                "Integrated Data Editor" },
                                                "EASY",
                                                "IDE stands for Integrated Development Environment."),

                                new QD("Which IDE is popular for Python?",
                                                new String[] { "PyCharm", "Photoshop", "AutoCAD", "Excel" },
                                                "EASY",
                                                "PyCharm is widely used for Python development."),

                                new QD("What does import do in Python?",
                                                new String[] { "Loads a module into your code", "Deletes code",
                                                                "Runs code", "Compiles code" },
                                                "EASY",
                                                "Import allows you to use external libraries."),

                                new QD("What is a package in Python?",
                                                new String[] { "A collection of modules", "A single variable", "A file",
                                                                "A function" },
                                                "EASY",
                                                "A package groups related modules."),

                                new QD("Where are pip packages stored?",
                                                new String[] { "In site-packages directory", "In root folder",
                                                                "In downloads", "In temp files" },
                                                "MEDIUM",
                                                "Packages are stored in the site-packages directory."),

                                new QD("What is a dependency?",
                                                new String[] { "A required library for a project", "A file extension",
                                                                "A variable", "A loop" },
                                                "MEDIUM",
                                                "Dependencies are required packages for your program."),

                                new QD("What does upgrading a package mean?",
                                                new String[] { "Installing a newer version", "Deleting it",
                                                                "Renaming it", "Moving it" },
                                                "MEDIUM",
                                                "Upgrading installs the latest version."),

                                new QD("Which command upgrades pandas?",
                                                new String[] { "pip install --upgrade pandas", "pip pandas upgrade",
                                                                "upgrade pandas", "pip update pandas" },
                                                "MEDIUM",
                                                "The correct flag is --upgrade."),

                                new QD("What is a virtual environment?",
                                                new String[] { "An isolated Python environment", "A cloud server",
                                                                "A database", "A browser tool" },
                                                "MEDIUM",
                                                "Virtual environments isolate dependencies."),

                                new QD("Why use virtual environments?",
                                                new String[] { "To manage dependencies separately",
                                                                "To increase CPU speed", "To run Java code",
                                                                "To compile programs" },
                                                "MEDIUM",
                                                "They prevent package conflicts."),

                                new QD("Which command creates a virtual environment?",
                                                new String[] { "python -m venv env", "create venv python",
                                                                "venv create", "python env make" },
                                                "MEDIUM",
                                                "python -m venv creates environments."),

                                new QD("Which command activates venv (Windows)?",
                                                new String[] { "env\\Scripts\\activate", "activate env", "start env",
                                                                "env start" },
                                                "MEDIUM",
                                                "That is the correct activation path."),

                                new QD("What happens if pandas is not installed?",
                                                new String[] { "Import error occurs", "Code runs normally",
                                                                "Program compiles", "Nothing happens" },
                                                "MEDIUM",
                                                "Python raises an ImportError."),

                                new QD("What is PATH in Python setup?",
                                                new String[] { "Environment variable for locating executables",
                                                                "A file type", "A package", "A library" },
                                                "HARD",
                                                "PATH helps the system find Python."),

                                new QD("What does python -m do?",
                                                new String[] { "Runs a module as a script", "Installs Python",
                                                                "Deletes modules", "Compiles code" },
                                                "HARD",
                                                "It executes modules directly."),

                                new QD("What happens if PATH is not set?",
                                                new String[] { "Python command won't work globally", "Code runs faster",
                                                                "Pandas fails only", "Nothing changes" },
                                                "HARD",
                                                "The system cannot find Python."),

                                new QD("What is pip freeze used for?",
                                                new String[] { "Lists installed packages", "Deletes packages",
                                                                "Installs packages", "Updates packages" },
                                                "HARD",
                                                "It outputs installed dependencies."),

                                new QD("Why use requirements.txt?",
                                                new String[] { "To replicate environments", "To compile code",
                                                                "To debug code", "To design UI" },
                                                "HARD",
                                                "It ensures consistent environments."),

                                new QD("What does pip uninstall do?",
                                                new String[] { "Removes a package", "Installs a package",
                                                                "Updates a package", "Renames a package" },
                                                "MEDIUM",
                                                "It deletes installed packages."),

                                new QD("Which command lists installed packages?",
                                                new String[] { "pip list", "pip show all", "list pip", "pip packages" },
                                                "MEDIUM",
                                                "pip list shows installed packages."),

                                new QD("What is Python interpreter?",
                                                new String[] { "Executes Python code", "Stores data", "Compiles Java",
                                                                "Runs SQL" },
                                                "EASY",
                                                "Interpreter runs Python code line by line."),

                                new QD("What is open-source software?",
                                                new String[] { "Software with accessible source code", "Paid software",
                                                                "Closed system", "Encrypted program" },
                                                "EASY",
                                                "Open-source allows modification."),

                                new QD("Why is Python popular in data science?",
                                                new String[] { "Simple syntax and strong libraries", "Fastest language",
                                                                "Best for hardware", "Used only in web" },
                                                "EASY",
                                                "Python is easy and has powerful libraries."),

                                new QD("What is a script?",
                                                new String[] { "A file containing Python code", "A database",
                                                                "A UI element", "A server" },
                                                "EASY",
                                                "Scripts are Python files."),

                                new QD("What is version control?",
                                                new String[] { "Tracking code changes", "Running code",
                                                                "Deleting files", "Installing libraries" },
                                                "MEDIUM",
                                                "It helps manage code history."),

                                new QD("Which tool is used for version control?",
                                                new String[] { "Git", "Excel", "Word", "Chrome" },
                                                "MEDIUM",
                                                "Git tracks changes in code."),

                                new QD("What does cloning a repo mean?",
                                                new String[] { "Copying a repository", "Deleting code", "Running code",
                                                                "Compiling code" },
                                                "MEDIUM",
                                                "Cloning creates a local copy."),

                                new QD("What is GitHub?",
                                                new String[] { "A platform for hosting code", "A database",
                                                                "A compiler", "An IDE" },
                                                "MEDIUM",
                                                "GitHub hosts repositories online."),

                                new QD("What is debugging?",
                                                new String[] { "Finding and fixing errors", "Writing code",
                                                                "Installing packages", "Running scripts" },
                                                "EASY",
                                                "Debugging removes errors."),

                                new QD("What is a syntax error?",
                                                new String[] { "Error in code structure", "Logic mistake",
                                                                "Hardware issue", "Network error" },
                                                "EASY",
                                                "Syntax errors break Python rules."),

                                new QD("What is a runtime error?",
                                                new String[] { "Error during execution", "Compile error",
                                                                "Syntax issue", "Install error" },
                                                "MEDIUM",
                                                "Occurs when code runs."),

                                new QD("What is indentation in Python?",
                                                new String[] { "Defines code blocks", "Adds comments", "Deletes code",
                                                                "Imports modules" },
                                                "EASY",
                                                "Indentation is required for structure."));
        }

        // =========================================================================
        // MODULE 2: Data Cleansing
        // =========================================================================

        private List<QD> dataCleansingQuestions() {
                return List.of(
                                new QD("What is data cleansing?",
                                                new String[] { "The process of correcting or removing inaccurate data",
                                                                "The process of visualizing data",
                                                                "The process of storing data",
                                                                "The process of encrypting data" },
                                                "EASY",
                                                "Data cleansing involves fixing or removing incorrect or messy data."),

                                new QD("Which pandas function removes missing values?",
                                                new String[] { "dropna()", "fillna()", "remove()", "clean()" },
                                                "EASY",
                                                "dropna() removes rows or columns with missing values."),

                                new QD("What does NaN stand for?",
                                                new String[] { "Not a Number", "New and Numeric", "Null and None",
                                                                "Number and Null" },
                                                "EASY",
                                                "NaN represents missing or undefined values."),

                                new QD("Which function fills missing values?",
                                                new String[] { "fillna()", "dropna()", "replace()", "clean()" },
                                                "EASY",
                                                "fillna() replaces missing values with a specified value."),

                                new QD("What is duplicate data?",
                                                new String[] { "Repeated records in a dataset", "Missing values",
                                                                "Sorted data", "Grouped data" },
                                                "EASY",
                                                "Duplicates are repeated rows or entries."),

                                new QD("Which function removes duplicates?",
                                                new String[] { "drop_duplicates()", "remove_duplicates()", "delete()",
                                                                "clean()" },
                                                "EASY",
                                                "drop_duplicates() removes repeated rows."),

                                new QD("What does df.isnull() do?",
                                                new String[] { "Checks for missing values", "Deletes null values",
                                                                "Fills null values", "Sorts data" },
                                                "EASY",
                                                "It returns True where values are missing."),

                                new QD("What does df.notnull() do?",
                                                new String[] { "Checks for non-missing values", "Deletes null values",
                                                                "Fills null values", "Sorts data" },
                                                "EASY",
                                                "It returns True where values exist."),

                                new QD("What is inconsistent data?",
                                                new String[] { "Data with different formats or representations",
                                                                "Duplicate data", "Missing data", "Sorted data" },
                                                "EASY",
                                                "Inconsistent data includes mismatched formats."),

                                new QD("What is an outlier in data?",
                                                new String[] { "A value significantly different from others",
                                                                "A duplicate value", "A missing value",
                                                                "A sorted value" },
                                                "EASY",
                                                "Outliers deviate from normal patterns."),

                                new QD("Which function replaces values?",
                                                new String[] { "replace()", "drop()", "fill()", "remove()" },
                                                "MEDIUM",
                                                "replace() substitutes values in a dataset."),

                                new QD("What does df.info() show?",
                                                new String[] { "Summary of DataFrame", "Deletes data", "Sorts data",
                                                                "Filters data" },
                                                "MEDIUM",
                                                "df.info() provides structure and data types."),

                                new QD("What does df.describe() do?",
                                                new String[] { "Provides statistical summary", "Deletes data",
                                                                "Sorts data", "Filters data" },
                                                "MEDIUM",
                                                "It shows mean, std, min, max etc."),

                                new QD("What is data normalization?",
                                                new String[] { "Scaling data to a standard range",
                                                                "Removing duplicates", "Sorting data",
                                                                "Deleting nulls" },
                                                "MEDIUM",
                                                "Normalization standardizes data values."),

                                new QD("What is data standardization?",
                                                new String[] { "Rescaling data to mean 0 and std 1",
                                                                "Deleting duplicates", "Sorting data",
                                                                "Removing nulls" },
                                                "MEDIUM",
                                                "Standardization centers data."),

                                new QD("Which method converts data types?",
                                                new String[] { "astype()", "convert()", "change()", "type()" },
                                                "MEDIUM",
                                                "astype() changes column data types."),

                                new QD("What is data type mismatch?",
                                                new String[] { "Incorrect data type assignment", "Missing value",
                                                                "Duplicate value", "Sorted value" },
                                                "MEDIUM",
                                                "Occurs when data types are inconsistent."),

                                new QD("What is trimming in data cleaning?",
                                                new String[] { "Removing whitespace", "Deleting rows", "Sorting data",
                                                                "Grouping data" },
                                                "MEDIUM",
                                                "Trimming removes extra spaces."),

                                new QD("Which method removes whitespace?",
                                                new String[] { "str.strip()", "str.remove()", "str.clean()",
                                                                "str.delete()" },
                                                "MEDIUM",
                                                "strip() removes leading/trailing spaces."),

                                new QD("What is case inconsistency?",
                                                new String[] { "Different text cases like upper/lower", "Missing data",
                                                                "Duplicate rows", "Sorted data" },
                                                "MEDIUM",
                                                "Case inconsistency affects comparisons."),

                                new QD("Which function converts to lowercase?",
                                                new String[] { "str.lower()", "str.down()", "str.small()",
                                                                "str.case()" },
                                                "MEDIUM",
                                                "lower() standardizes text."),

                                new QD("Which function converts to uppercase?",
                                                new String[] { "str.upper()", "str.up()", "str.big()", "str.case()" },
                                                "MEDIUM",
                                                "upper() converts text to uppercase."),

                                new QD("What is data validation?",
                                                new String[] { "Ensuring data accuracy and quality", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Validation ensures correctness."),

                                new QD("What is missing data imputation?",
                                                new String[] { "Filling missing values with estimates", "Deleting rows",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Imputation replaces missing values."),

                                new QD("Which value is commonly used for imputation?",
                                                new String[] { "Mean", "Random text", "Zero always",
                                                                "Duplicate values" },
                                                "MEDIUM",
                                                "Mean is commonly used for numerical data."),

                                new QD("What is forward fill?",
                                                new String[] { "Fill missing values with previous value",
                                                                "Delete missing values", "Sort values",
                                                                "Group values" },
                                                "HARD",
                                                "Forward fill propagates last valid value."),

                                new QD("What is backward fill?",
                                                new String[] { "Fill missing values with next value",
                                                                "Delete missing values", "Sort values",
                                                                "Group values" },
                                                "HARD",
                                                "Backward fill uses next valid value."),

                                new QD("Which parameter is used in fillna for forward fill?",
                                                new String[] { "method='ffill'", "method='forward'", "method='next'",
                                                                "method='prev'" },
                                                "HARD",
                                                "ffill stands for forward fill."),

                                new QD("What is data consistency?",
                                                new String[] { "Uniform data format across dataset", "Duplicate data",
                                                                "Missing data", "Sorted data" },
                                                "HARD",
                                                "Consistency ensures uniform structure."),

                                new QD("What is schema validation?",
                                                new String[] { "Checking data structure matches expected format",
                                                                "Deleting data", "Sorting data", "Grouping data" },
                                                "HARD",
                                                "Schema validation ensures correct structure."),

                                new QD("What is data integrity?",
                                                new String[] { "Accuracy and reliability of data", "Duplicate removal",
                                                                "Sorting data", "Grouping data" },
                                                "HARD",
                                                "Integrity ensures data correctness."),

                                new QD("Which function detects duplicates?",
                                                new String[] { "duplicated()", "duplicates()", "finddup()",
                                                                "checkdup()" },
                                                "MEDIUM",
                                                "duplicated() flags duplicate rows."),

                                new QD("What does inplace=True do?",
                                                new String[] { "Modifies original data", "Creates copy", "Deletes data",
                                                                "Sorts data" },
                                                "MEDIUM",
                                                "It updates data without creating new object."),

                                new QD("What is a null value?",
                                                new String[] { "Missing data", "Duplicate data", "Sorted data",
                                                                "Grouped data" },
                                                "EASY",
                                                "Null means no value present."),

                                new QD("What is data wrangling?",
                                                new String[] { "Cleaning and transforming data", "Sorting data",
                                                                "Deleting data", "Grouping data" },
                                                "EASY",
                                                "Wrangling prepares data for analysis."),

                                new QD("What is a dataset?",
                                                new String[] { "Collection of data", "Single value", "Variable",
                                                                "Function" },
                                                "EASY",
                                                "Dataset is structured data."),

                                new QD("What is a column in a dataset?",
                                                new String[] { "A field of data", "A row", "A function", "A file" },
                                                "EASY",
                                                "Column represents attribute."),

                                new QD("What is a row in a dataset?",
                                                new String[] { "A single record", "A column", "A function",
                                                                "A variable" },
                                                "EASY",
                                                "Row represents one entry."),

                                new QD("What is filtering invalid data?",
                                                new String[] { "Removing incorrect values", "Sorting data",
                                                                "Grouping data", "Duplicating data" },
                                                "EASY",
                                                "Invalid data must be removed."),

                                new QD("What is noise in data?",
                                                new String[] { "Random errors or irrelevant data", "Duplicate data",
                                                                "Missing data", "Sorted data" },
                                                "EASY",
                                                "Noise reduces data quality."),

                                new QD("What is data transformation?",
                                                new String[] { "Changing data format or structure", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Transformation reshapes data."),

                                new QD("What is encoding categorical data?",
                                                new String[] { "Converting text to numbers", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Encoding allows numerical processing."),

                                new QD("Which method encodes categories?",
                                                new String[] { "get_dummies()", "encode()", "convert()", "mapdata()" },
                                                "MEDIUM",
                                                "get_dummies() creates dummy variables."),

                                new QD("What is label encoding?",
                                                new String[] { "Assigning numeric labels", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Each category gets a number."),

                                new QD("What is one-hot encoding?",
                                                new String[] { "Creating binary columns", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Each category becomes a column."),

                                new QD("What is the goal of data cleaning?",
                                                new String[] { "Improve data quality", "Delete all data", "Sort data",
                                                                "Group data" },
                                                "EASY",
                                                "Goal is accurate and usable data."));
        }

        // =========================================================================
        // MODULE 3: Filtering and Grouping
        // =========================================================================

        private List<QD> filteringAndGroupingQuestions() {
                return List.of(
                                new QD("What is filtering in pandas?",
                                                new String[] { "Selecting specific rows based on conditions",
                                                                "Sorting data", "Deleting data", "Grouping data" },
                                                "EASY",
                                                "Filtering allows you to select rows that meet certain conditions."),

                                new QD("Which operator is used for filtering equality?",
                                                new String[] { "==", "=", "!=", ":" },
                                                "EASY",
                                                "== is used to compare values in filtering."),

                                new QD("How do you filter rows in pandas?",
                                                new String[] { "Using boolean conditions", "Using loops", "Using print",
                                                                "Using sort" },
                                                "EASY",
                                                "Boolean conditions are used to filter data."),

                                new QD("What does df[df['col'] > 5] do?",
                                                new String[] { "Filters rows where column value is greater than 5",
                                                                "Sorts data", "Deletes data", "Groups data" },
                                                "EASY",
                                                "It returns rows meeting the condition."),

                                new QD("What is boolean indexing?",
                                                new String[] { "Using True/False conditions to filter data",
                                                                "Sorting data", "Deleting data", "Grouping data" },
                                                "EASY",
                                                "Boolean indexing uses conditions to select data."),

                                new QD("Which symbol represents AND condition in pandas?",
                                                new String[] { "&", "&&", "|", "and" },
                                                "EASY",
                                                "& is used for AND in pandas conditions."),

                                new QD("Which symbol represents OR condition in pandas?",
                                                new String[] { "|", "||", "&", "or" },
                                                "EASY",
                                                "| is used for OR in pandas."),

                                new QD("What does df.loc[] do?",
                                                new String[] { "Access rows and columns by labels", "Delete data",
                                                                "Sort data", "Group data" },
                                                "EASY",
                                                "loc is used for label-based selection."),

                                new QD("What does df.iloc[] do?",
                                                new String[] { "Access rows and columns by index", "Delete data",
                                                                "Sort data", "Group data" },
                                                "EASY",
                                                "iloc uses integer positions."),

                                new QD("What is grouping in pandas?",
                                                new String[] { "Combining data based on categories", "Filtering data",
                                                                "Sorting data", "Deleting data" },
                                                "EASY",
                                                "Grouping organizes data into categories."),

                                new QD("Which function is used for grouping?",
                                                new String[] { "groupby()", "filter()", "sort()", "combine()" },
                                                "EASY",
                                                "groupby() groups data."),

                                new QD("What does groupby() return?",
                                                new String[] { "A grouped object for aggregation", "Sorted data",
                                                                "Deleted data", "Filtered data" },
                                                "MEDIUM",
                                                "groupby creates grouped data for aggregation."),

                                new QD("What is aggregation?",
                                                new String[] { "Summarizing grouped data", "Filtering data",
                                                                "Deleting data", "Sorting data" },
                                                "MEDIUM",
                                                "Aggregation computes summary statistics."),

                                new QD("Which function calculates the mean?",
                                                new String[] { "mean()", "avg()", "sum()", "calc()" },
                                                "MEDIUM",
                                                "mean() calculates average."),

                                new QD("Which function calculates the sum?",
                                                new String[] { "sum()", "add()", "total()", "calc()" },
                                                "MEDIUM",
                                                "sum() adds values."),

                                new QD("What does count() do in groupby?",
                                                new String[] { "Counts non-null values", "Deletes data", "Sorts data",
                                                                "Filters data" },
                                                "MEDIUM",
                                                "count() returns number of non-null entries."),

                                new QD("What is multi-level grouping?",
                                                new String[] { "Grouping by multiple columns", "Filtering data",
                                                                "Sorting data", "Deleting data" },
                                                "MEDIUM",
                                                "You can group by more than one column."),

                                new QD("How do you group by multiple columns?",
                                                new String[] { "groupby(['col1','col2'])", "group(col1,col2)",
                                                                "group(col1)", "multiGroup()" },
                                                "MEDIUM",
                                                "Pass a list of columns."),

                                new QD("What does size() do in groupby?",
                                                new String[] { "Returns group sizes", "Deletes data", "Sorts data",
                                                                "Filters data" },
                                                "MEDIUM",
                                                "size() counts rows in each group."),

                                new QD("What does agg() do?",
                                                new String[] { "Applies multiple aggregations", "Deletes data",
                                                                "Sorts data", "Filters data" },
                                                "MEDIUM",
                                                "agg() allows multiple functions."),

                                new QD("What is transformation in groupby?",
                                                new String[] { "Returns transformed data of same size", "Deletes data",
                                                                "Sorts data", "Filters data" },
                                                "HARD",
                                                "Transform keeps original shape."),

                                new QD("What does apply() do in groupby?",
                                                new String[] { "Applies custom function to groups", "Deletes data",
                                                                "Sorts data", "Filters data" },
                                                "HARD",
                                                "apply() allows custom operations."),

                                new QD("What is a pivot table?",
                                                new String[] { "Summarized table based on grouping", "Filtered data",
                                                                "Sorted data", "Deleted data" },
                                                "HARD",
                                                "Pivot tables summarize grouped data."),

                                new QD("Which function creates a pivot table?",
                                                new String[] { "pivot_table()", "pivot()", "table()", "groupPivot()" },
                                                "HARD",
                                                "pivot_table() is used."),

                                new QD("What is sorting?",
                                                new String[] { "Arranging data in order", "Filtering data",
                                                                "Deleting data", "Grouping data" },
                                                "EASY",
                                                "Sorting organizes data."),

                                new QD("Which function sorts values?",
                                                new String[] { "sort_values()", "sort()", "order()", "arrange()" },
                                                "EASY",
                                                "sort_values() sorts by column."),

                                new QD("Which function sorts by index?",
                                                new String[] { "sort_index()", "sort()", "order()", "arrange()" },
                                                "EASY",
                                                "sort_index() sorts by index."),

                                new QD("What does ascending=True mean in sorting?",
                                                new String[] { "Sort from lowest to highest",
                                                                "Sort from highest to lowest", "Delete data",
                                                                "Filter data" },
                                                "EASY",
                                                "Ascending sorts smallest first."),

                                new QD("What does ascending=False mean in sorting?",
                                                new String[] { "Sort from highest to lowest",
                                                                "Sort from lowest to highest", "Delete data",
                                                                "Filter data" },
                                                "EASY",
                                                "Descending sorts largest first."),

                                new QD("What is conditional filtering?",
                                                new String[] { "Filtering based on conditions", "Sorting data",
                                                                "Deleting data", "Grouping data" },
                                                "EASY",
                                                "Uses logical conditions."),

                                new QD("What is chaining filters?",
                                                new String[] { "Combining multiple conditions", "Sorting data",
                                                                "Deleting data", "Grouping data" },
                                                "MEDIUM",
                                                "You can combine conditions using & or |."),

                                new QD("What is an index in pandas?",
                                                new String[] { "Labels for rows", "Column values", "Functions",
                                                                "Variables" },
                                                "EASY",
                                                "Index identifies rows."),

                                new QD("What is column selection?",
                                                new String[] { "Selecting specific columns", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "EASY",
                                                "You can select columns using brackets."),

                                new QD("What does df['col'] return?",
                                                new String[] { "A single column as Series", "Deletes column",
                                                                "Sorts column", "Groups column" },
                                                "EASY",
                                                "It accesses a column."),

                                new QD("What is slicing?",
                                                new String[] { "Selecting subset of data", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Slicing extracts parts of data."),

                                new QD("What does head() do?",
                                                new String[] { "Shows first rows", "Deletes rows", "Sorts rows",
                                                                "Filters rows" },
                                                "EASY",
                                                "head() displays top rows."),

                                new QD("What does tail() do?",
                                                new String[] { "Shows last rows", "Deletes rows", "Sorts rows",
                                                                "Filters rows" },
                                                "EASY",
                                                "tail() shows bottom rows."),

                                new QD("What does unique() do?",
                                                new String[] { "Returns unique values", "Deletes values",
                                                                "Sorts values", "Groups values" },
                                                "MEDIUM",
                                                "unique() finds distinct values."),

                                new QD("What does value_counts() do?",
                                                new String[] { "Counts unique values", "Deletes values", "Sorts values",
                                                                "Groups values" },
                                                "MEDIUM",
                                                "It shows frequency of values."),

                                new QD("What is filtering by string?",
                                                new String[] { "Using string conditions to filter", "Sorting strings",
                                                                "Deleting strings", "Grouping strings" },
                                                "MEDIUM",
                                                "String filtering uses conditions like contains."),

                                new QD("Which method checks for a substring?",
                                                new String[] { "str.contains()", "str.find()", "str.check()",
                                                                "str.search()" },
                                                "MEDIUM",
                                                "contains() checks substring."),

                                new QD("What is the purpose of grouping?",
                                                new String[] { "Summarize data by category", "Filter data",
                                                                "Delete data", "Sort data" },
                                                "EASY",
                                                "Grouping helps analysis."),

                                new QD("What is a hierarchical index?",
                                                new String[] { "Multi-level index", "Single index", "Sorted index",
                                                                "Deleted index" },
                                                "HARD",
                                                "Hierarchical index has multiple levels."),

                                new QD("What does reset_index() do?",
                                                new String[] { "Resets index to default", "Deletes index",
                                                                "Sorts index", "Groups index" },
                                                "MEDIUM",
                                                "It converts index to column."),

                                new QD("What does set_index() do?",
                                                new String[] { "Sets column as index", "Deletes column", "Sorts column",
                                                                "Groups column" },
                                                "MEDIUM",
                                                "It assigns index."),

                                new QD("What is filtering efficiency?",
                                                new String[] { "Speed of filtering operations", "Sorting speed",
                                                                "Deleting speed", "Grouping speed" },
                                                "HARD",
                                                "Efficient filtering improves performance."));
        }

        // =========================================================================
        // MODULE 4: Visualisation with Pandas
        // =========================================================================

        private List<QD> visualisationWithPandasQuestions() {
                return List.of(
                                new QD("What is data visualization?",
                                                new String[] { "Graphical representation of data", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "EASY",
                                                "Visualization helps understand data through charts and graphs."),

                                new QD("Which library is commonly used with pandas for plotting?",
                                                new String[] { "Matplotlib", "NumPy", "SciPy", "TensorFlow" },
                                                "EASY",
                                                "Matplotlib is widely used for plotting."),

                                new QD("Which function plots data in pandas?",
                                                new String[] { "plot()", "draw()", "graph()", "visualize()" },
                                                "EASY",
                                                "plot() is used to create graphs."),

                                new QD("What is a line plot?",
                                                new String[] { "Graph showing data trends over time", "Bar chart",
                                                                "Pie chart", "Histogram" },
                                                "EASY",
                                                "Line plots show trends."),

                                new QD("What is a bar chart?",
                                                new String[] { "Graph using bars to compare values", "Line graph",
                                                                "Pie chart", "Scatter plot" },
                                                "EASY",
                                                "Bar charts compare categories."),

                                new QD("What is a histogram?",
                                                new String[] { "Distribution of numerical data", "Line chart",
                                                                "Pie chart", "Scatter plot" },
                                                "EASY",
                                                "Histogram shows frequency distribution."),

                                new QD("What is a scatter plot?",
                                                new String[] { "Shows relationship between two variables", "Bar chart",
                                                                "Pie chart", "Line chart" },
                                                "EASY",
                                                "Scatter plots show correlation."),

                                new QD("Which argument sets the plot type?",
                                                new String[] { "kind", "type", "style", "plotType" },
                                                "EASY",
                                                "kind specifies the chart type."),

                                new QD("What does kind='bar' produce?",
                                                new String[] { "A bar chart", "A line chart", "A histogram",
                                                                "A scatter plot" },
                                                "EASY",
                                                "It generates a bar chart."),

                                new QD("What does kind='line' produce?",
                                                new String[] { "A line plot", "A bar chart", "A histogram",
                                                                "A scatter plot" },
                                                "EASY",
                                                "It generates a line graph."),

                                new QD("What does kind='hist' produce?",
                                                new String[] { "A histogram", "A bar chart", "A scatter plot",
                                                                "A line chart" },
                                                "EASY",
                                                "It plots distribution."),

                                new QD("What does kind='scatter' require?",
                                                new String[] { "Two variables (x and y)", "One variable",
                                                                "Three variables", "No variables" },
                                                "EASY",
                                                "Scatter needs x and y."),

                                new QD("What is the x-axis?",
                                                new String[] { "Horizontal axis", "Vertical axis", "Data point",
                                                                "Legend" },
                                                "EASY",
                                                "X-axis is horizontal."),

                                new QD("What is the y-axis?",
                                                new String[] { "Vertical axis", "Horizontal axis", "Data point",
                                                                "Legend" },
                                                "EASY",
                                                "Y-axis is vertical."),

                                new QD("What is a title in a chart?",
                                                new String[] { "Description of the chart", "Axis", "Legend", "Grid" },
                                                "EASY",
                                                "Title explains the chart."),

                                new QD("Which method sets the chart title in matplotlib?",
                                                new String[] { "set_title()", "title()", "add_title()", "name()" },
                                                "MEDIUM",
                                                "set_title() is used with matplotlib axes."),

                                new QD("What is a legend?",
                                                new String[] { "Explains symbols or colors in a chart", "Axis", "Title",
                                                                "Grid" },
                                                "EASY",
                                                "Legend identifies data series."),

                                new QD("Which method adds a legend?",
                                                new String[] { "legend()", "addLegend()", "showLegend()", "label()" },
                                                "MEDIUM",
                                                "legend() displays the legend."),

                                new QD("What is a grid in a plot?",
                                                new String[] { "Lines for easier reading", "Title", "Axis", "Legend" },
                                                "EASY",
                                                "Grid improves readability."),

                                new QD("What does figsize control?",
                                                new String[] { "Figure size", "Data size", "Font size", "Axis size" },
                                                "MEDIUM",
                                                "figsize sets plot dimensions."),

                                new QD("What is the color parameter used for?",
                                                new String[] { "Sets color of plot elements", "Sets size", "Sets axis",
                                                                "Sets title" },
                                                "EASY",
                                                "Color customizes appearance."),

                                new QD("What is a marker in a plot?",
                                                new String[] { "Symbol for data points", "Axis", "Title", "Legend" },
                                                "MEDIUM",
                                                "Markers highlight points."),

                                new QD("What does style affect in plots?",
                                                new String[] { "Visual appearance", "Data type", "Axis type",
                                                                "Legend type" },
                                                "MEDIUM",
                                                "Style affects design."),

                                new QD("What is a subplot?",
                                                new String[] { "Multiple plots in one figure", "Single plot",
                                                                "Data grouping", "Filtering" },
                                                "HARD",
                                                "Subplots combine charts."),

                                new QD("What does plt.show() do?",
                                                new String[] { "Displays the plot", "Deletes plot", "Saves plot",
                                                                "Sorts plot" },
                                                "EASY",
                                                "It renders the chart."),

                                new QD("What does savefig() do?",
                                                new String[] { "Saves plot as a file", "Displays plot", "Deletes plot",
                                                                "Sorts plot" },
                                                "MEDIUM",
                                                "It exports the chart."),

                                new QD("What is a pie chart?",
                                                new String[] { "Shows proportions of a whole", "Shows trends",
                                                                "Shows distribution", "Shows correlation" },
                                                "EASY",
                                                "Pie charts show percentages."),

                                new QD("Which kind value creates a pie chart?",
                                                new String[] { "pie", "circle", "round", "portion" },
                                                "EASY",
                                                "kind='pie' creates pie chart."),

                                new QD("What is a box plot?",
                                                new String[] { "Shows distribution and outliers", "Line chart",
                                                                "Bar chart", "Pie chart" },
                                                "MEDIUM",
                                                "Box plot shows spread."),

                                new QD("What is an area plot?",
                                                new String[] { "Filled line plot", "Bar chart", "Scatter plot",
                                                                "Pie chart" },
                                                "MEDIUM",
                                                "Area plots emphasize magnitude."),

                                new QD("What is a correlation plot?",
                                                new String[] { "Shows relationships between variables", "Bar chart",
                                                                "Pie chart", "Histogram" },
                                                "HARD",
                                                "It visualizes correlation."),

                                new QD("What is a heatmap?",
                                                new String[] { "Color-coded data representation", "Line chart",
                                                                "Bar chart", "Pie chart" },
                                                "HARD",
                                                "Heatmaps show intensity."),

                                new QD("What is an axis label?",
                                                new String[] { "Description of an axis", "Title", "Legend", "Grid" },
                                                "EASY",
                                                "Labels describe axes."),

                                new QD("Which method sets the x-axis label?",
                                                new String[] { "xlabel()", "setx()", "labelx()", "xname()" },
                                                "MEDIUM",
                                                "xlabel sets x-axis name."),

                                new QD("Which method sets the y-axis label?",
                                                new String[] { "ylabel()", "sety()", "labely()", "yname()" },
                                                "MEDIUM",
                                                "ylabel sets y-axis name."),

                                new QD("What is a tick on an axis?",
                                                new String[] { "Mark indicating a value on the axis", "Legend", "Title",
                                                                "Grid" },
                                                "MEDIUM",
                                                "Ticks indicate values."),

                                new QD("What does rotation do to axis labels?",
                                                new String[] { "Changes the angle of labels", "Deletes labels",
                                                                "Sorts labels", "Colors labels" },
                                                "MEDIUM",
                                                "Rotation improves readability."),

                                new QD("What does the alpha parameter control?",
                                                new String[] { "Opacity level of plot elements", "Color type",
                                                                "Axis type", "Legend type" },
                                                "MEDIUM",
                                                "Alpha controls transparency."),

                                new QD("What is plotting multiple columns?",
                                                new String[] { "Plotting several data series on one chart",
                                                                "Deleting data", "Sorting data", "Grouping data" },
                                                "MEDIUM",
                                                "Multiple columns can be plotted together."),

                                new QD("What is a stacked bar chart?",
                                                new String[] { "Bars stacked on top of each other", "Separate bars",
                                                                "Line chart", "Scatter plot" },
                                                "MEDIUM",
                                                "Stacked bars show composition."),

                                new QD("What is a density plot?",
                                                new String[] { "Smoothed histogram", "Bar chart", "Pie chart",
                                                                "Line chart" },
                                                "HARD",
                                                "Density shows distribution shape."),

                                new QD("What is visual clarity in charts?",
                                                new String[] { "Ease of understanding the chart", "Data size",
                                                                "Axis type", "Legend type" },
                                                "EASY",
                                                "Clear visuals improve understanding."),

                                new QD("What is overplotting?",
                                                new String[] { "Too many overlapping data points", "Sorting data",
                                                                "Deleting data", "Grouping data" },
                                                "HARD",
                                                "Overplotting reduces readability."),

                                new QD("What is chart customization?",
                                                new String[] { "Modifying visual elements of a chart", "Deleting data",
                                                                "Sorting data", "Grouping data" },
                                                "EASY",
                                                "Customization improves presentation."),

                                new QD("What is an annotation in a plot?",
                                                new String[] { "Adding notes or labels to a plot", "Deleting plot",
                                                                "Sorting plot", "Grouping plot" },
                                                "MEDIUM",
                                                "Annotations explain details."),

                                new QD("What is a dashboard?",
                                                new String[] { "A collection of visualizations", "A single plot",
                                                                "A dataset", "A function" },
                                                "EASY",
                                                "Dashboards combine charts."),

                                new QD("What is storytelling with data?",
                                                new String[] { "Using visuals to communicate insights", "Sorting data",
                                                                "Deleting data", "Grouping data" },
                                                "EASY",
                                                "Visualization communicates insights."));
        }

        // =========================================================================
        // MODULE 5: Foundations of Management
        // =========================================================================

        private List<QD> foundationsOfManagementQuestions() {
                return List.of(
                                new QD("What is management primarily concerned with?",
                                                new String[] { "Achieving goals efficiently and effectively",
                                                                "Hiring employees", "Increasing profits only",
                                                                "Reducing costs" },
                                                "EASY",
                                                "Management focuses on achieving organisational goals efficiently and effectively."),

                                new QD("Which of the following is a core management function?",
                                                new String[] { "Planning", "Coding", "Marketing", "Accounting" },
                                                "EASY",
                                                "Planning is one of the four core functions of management."),

                                new QD("What does 'efficiency' in management mean?",
                                                new String[] { "Using resources wisely", "Working faster",
                                                                "Hiring more staff", "Avoiding planning" },
                                                "EASY",
                                                "Efficiency is about minimising waste and using resources effectively."),

                                new QD("What does 'effectiveness' refer to?",
                                                new String[] { "Achieving desired goals", "Reducing staff",
                                                                "Saving money", "Working overtime" },
                                                "EASY",
                                                "Effectiveness means accomplishing organisational objectives."),

                                new QD("Which function involves setting objectives?",
                                                new String[] { "Planning", "Leading", "Controlling", "Staffing" },
                                                "EASY",
                                                "Planning involves defining goals and determining how to achieve them."),

                                new QD("What is organising in management?",
                                                new String[] { "Arranging tasks and resources", "Hiring employees",
                                                                "Monitoring work", "Setting salaries" },
                                                "EASY",
                                                "Organising ensures tasks and resources are structured properly."),

                                new QD("Which function involves motivating employees?",
                                                new String[] { "Leading", "Planning", "Controlling", "Budgeting" },
                                                "EASY",
                                                "Leading focuses on guiding and motivating team members."),

                                new QD("What is controlling in management?",
                                                new String[] { "Monitoring performance and making corrections",
                                                                "Hiring staff", "Giving rewards", "Planning tasks" },
                                                "EASY",
                                                "Controlling ensures performance aligns with goals."),

                                new QD("What is a manager?",
                                                new String[] { "Someone who coordinates and oversees work",
                                                                "A team member", "A customer", "A supplier" },
                                                "EASY",
                                                "Managers are responsible for coordinating organisational activities."),

                                new QD("Which level of management focuses on daily operations?",
                                                new String[] { "Lower-level management", "Top-level management",
                                                                "Middle management", "External consultants" },
                                                "EASY",
                                                "Lower-level managers supervise day-to-day activities."),

                                new QD("Which level sets organisational strategy?",
                                                new String[] { "Top-level management", "Middle management",
                                                                "Lower-level management", "Employees" },
                                                "MEDIUM",
                                                "Top managers define long-term goals and strategies."),

                                new QD("What do middle managers mainly do?",
                                                new String[] { "Implement strategies", "Create company vision",
                                                                "Handle customer complaints only", "Develop software" },
                                                "MEDIUM",
                                                "They translate top-level strategy into actionable plans."),

                                new QD("What is decision making?",
                                                new String[] { "Choosing between alternatives", "Hiring employees",
                                                                "Budgeting", "Leading meetings" },
                                                "EASY",
                                                "Decision making involves selecting the best course of action."),

                                new QD("What is a goal?",
                                                new String[] { "A desired outcome", "A random idea",
                                                                "A financial report", "A team meeting" },
                                                "EASY",
                                                "Goals define what an organisation aims to achieve."),

                                new QD("What is a strategy?",
                                                new String[] { "A plan to achieve long-term goals", "A daily task",
                                                                "A meeting agenda", "A report" },
                                                "MEDIUM",
                                                "Strategy outlines how objectives will be achieved over time."),

                                new QD("Which skill is most important for top managers?",
                                                new String[] { "Conceptual skills", "Technical skills", "Manual skills",
                                                                "Typing skills" },
                                                "MEDIUM",
                                                "Top managers need strong conceptual thinking abilities."),

                                new QD("Technical skills are most important for which managers?",
                                                new String[] { "Lower-level managers", "Top-level managers",
                                                                "Customers", "Suppliers" },
                                                "MEDIUM",
                                                "Lower-level managers require technical expertise."),

                                new QD("What are human skills?",
                                                new String[] { "Ability to work with people", "Programming ability",
                                                                "Financial skills", "Typing speed" },
                                                "EASY",
                                                "Human skills involve communication and teamwork."),

                                new QD("What is an organisation?",
                                                new String[] { "A group of people working toward common goals",
                                                                "A single individual", "A building", "A product" },
                                                "EASY",
                                                "Organisations consist of people working together."),

                                new QD("What is productivity?",
                                                new String[] { "Output relative to input", "Total revenue",
                                                                "Employee count", "Working hours" },
                                                "MEDIUM",
                                                "Productivity measures efficiency in using resources."),

                                new QD("Which management function comes first?",
                                                new String[] { "Planning", "Organising", "Leading", "Controlling" },
                                                "EASY",
                                                "Planning is the starting point of management."),

                                new QD("What is delegation?",
                                                new String[] { "Assigning tasks to others", "Avoiding work",
                                                                "Working overtime", "Hiring staff" },
                                                "EASY",
                                                "Delegation allows managers to distribute responsibilities."),

                                new QD("What is authority?",
                                                new String[] { "The right to make decisions", "A suggestion",
                                                                "A reward", "A punishment" },
                                                "EASY",
                                                "Authority gives managers power to act."),

                                new QD("What is accountability?",
                                                new String[] { "Being responsible for outcomes", "Delegating tasks",
                                                                "Planning work", "Leading meetings" },
                                                "EASY",
                                                "Accountability means answering for results."),

                                new QD("What is span of control?",
                                                new String[] { "Number of employees a manager supervises",
                                                                "Company size", "Revenue amount", "Work hours" },
                                                "MEDIUM",
                                                "Span of control defines how many employees report to a manager."),

                                new QD("What does centralisation mean?",
                                                new String[] { "Decision-making concentrated at top",
                                                                "Everyone makes decisions", "No structure exists",
                                                                "Employees lead" },
                                                "HARD",
                                                "Centralisation keeps authority at higher levels."),

                                new QD("What does decentralisation mean?",
                                                new String[] { "Decision-making spread across levels",
                                                                "Only top decides", "No decisions made",
                                                                "Customers decide" },
                                                "HARD",
                                                "Decentralisation distributes authority."),

                                new QD("What is a mission statement?",
                                                new String[] { "Purpose of the organisation", "Financial report",
                                                                "Employee contract", "Daily schedule" },
                                                "EASY",
                                                "Mission defines why an organisation exists."),

                                new QD("What is a vision statement?",
                                                new String[] { "Future aspirations of the organisation", "Daily tasks",
                                                                "Past performance", "Employee roles" },
                                                "EASY",
                                                "Vision outlines long-term goals."),

                                new QD("What is organisational culture?",
                                                new String[] { "Shared values and beliefs", "Office design",
                                                                "Dress code only", "Working hours" },
                                                "MEDIUM",
                                                "Culture influences behaviour within organisations."),

                                new QD("What is a planning horizon?",
                                                new String[] { "Time span for planning", "Work schedule",
                                                                "Employee shift", "Budget period" },
                                                "MEDIUM",
                                                "It defines how far ahead planning occurs."),

                                new QD("What is a policy?",
                                                new String[] { "Guideline for decision making", "A rulebook",
                                                                "A contract", "A law" },
                                                "MEDIUM",
                                                "Policies guide consistent decisions."),

                                new QD("What is a procedure?",
                                                new String[] { "Step-by-step instructions", "General guideline",
                                                                "Strategy", "Goal" },
                                                "EASY",
                                                "Procedures provide detailed steps."),

                                new QD("What is a budget?",
                                                new String[] { "A financial plan", "A daily report",
                                                                "An employee schedule", "A strategy" },
                                                "EASY",
                                                "Budgets outline expected income and expenses."),

                                new QD("What is SWOT analysis used for?",
                                                new String[] { "Evaluating strengths, weaknesses, opportunities, threats",
                                                                "Hiring", "Budgeting", "Training" },
                                                "MEDIUM",
                                                "SWOT helps strategic planning."),

                                new QD("What is benchmarking?",
                                                new String[] { "Comparing performance with best practices",
                                                                "Hiring staff", "Budgeting", "Planning meetings" },
                                                "HARD",
                                                "Benchmarking improves performance by comparison."),

                                new QD("What is risk management?",
                                                new String[] { "Identifying and managing risks", "Avoiding work",
                                                                "Hiring staff", "Budgeting" },
                                                "HARD",
                                                "It helps reduce uncertainty."),

                                new QD("What is forecasting?",
                                                new String[] { "Predicting future conditions", "Hiring staff",
                                                                "Planning meetings", "Budgeting only" },
                                                "HARD",
                                                "Forecasting supports planning."),

                                new QD("What is organisational structure?",
                                                new String[] { "How tasks are divided and coordinated", "Office design",
                                                                "Company culture", "Salary levels" },
                                                "HARD",
                                                "Structure defines roles and relationships."),

                                new QD("What is line authority?",
                                                new String[] { "Direct authority over subordinates", "Advisory role",
                                                                "External control", "Temporary authority" },
                                                "HARD",
                                                "Line managers have direct control."),

                                new QD("What is staff authority?",
                                                new String[] { "Advisory support role", "Direct control",
                                                                "Customer role", "Financial role" },
                                                "HARD",
                                                "Staff roles provide expertise."),

                                new QD("What is coordination?",
                                                new String[] { "Aligning activities to achieve goals", "Hiring staff",
                                                                "Budgeting", "Controlling" },
                                                "EASY",
                                                "Coordination ensures teamwork."),

                                new QD("What is innovation in management?",
                                                new String[] { "Introducing new ideas", "Following rules",
                                                                "Avoiding change", "Maintaining status quo" },
                                                "MEDIUM",
                                                "Innovation drives growth."),

                                new QD("What is adaptability?",
                                                new String[] { "Ability to adjust to change", "Avoiding change",
                                                                "Working slower", "Following rules strictly" },
                                                "EASY",
                                                "Adaptability helps organisations survive."),

                                new QD("What is competitive advantage?",
                                                new String[] { "Edge over competitors", "Higher costs",
                                                                "More employees", "Longer hours" },
                                                "MEDIUM",
                                                "It helps organisations outperform others."),

                                new QD("What is ethics in management?",
                                                new String[] { "Moral principles guiding decisions", "Company rules",
                                                                "Legal contracts", "Policies" },
                                                "MEDIUM",
                                                "Ethics influence responsible behaviour."),

                                new QD("What is corporate social responsibility?",
                                                new String[] { "Business responsibility to society",
                                                                "Profit maximisation only", "Employee hiring",
                                                                "Marketing" },
                                                "MEDIUM",
                                                "CSR focuses on social impact."),

                                new QD("What is globalisation in management?",
                                                new String[] { "Operating across countries", "Local hiring",
                                                                "Domestic sales only", "Small teams" },
                                                "MEDIUM",
                                                "Globalisation expands business reach."),

                                new QD("What is a stakeholder?",
                                                new String[] { "Anyone affected by the organisation", "Only employees",
                                                                "Only managers", "Only customers" },
                                                "EASY",
                                                "Stakeholders include all interested parties."),

                                new QD("What is performance management?",
                                                new String[] { "Monitoring and improving performance", "Hiring staff",
                                                                "Budgeting", "Planning meetings" },
                                                "MEDIUM",
                                                "It ensures continuous improvement."),

                                new QD("What is continuous improvement?",
                                                new String[] { "Ongoing enhancement of processes", "One-time change",
                                                                "Avoiding change", "Hiring staff" },
                                                "MEDIUM",
                                                "Continuous improvement focuses on gradual progress."));
        }

        // =========================================================================
        // MODULE 6: Leadership and Team Dynamics
        // =========================================================================

        private List<QD> leadershipAndTeamDynamicsQuestions() {
                return List.of(
                                new QD("What is leadership?",
                                                new String[] { "The ability to influence and guide others",
                                                                "Managing finances", "Completing tasks alone",
                                                                "Following instructions" },
                                                "EASY",
                                                "Leadership involves influencing and motivating others toward goals."),

                                new QD("What is a team?",
                                                new String[] { "A group working toward a common goal",
                                                                "A single individual", "A random group",
                                                                "A set of tasks" },
                                                "EASY",
                                                "Teams consist of individuals collaborating to achieve shared objectives."),

                                new QD("What is teamwork?",
                                                new String[] { "Collaborative effort of a group",
                                                                "Working independently", "Competing with others",
                                                                "Avoiding responsibility" },
                                                "EASY",
                                                "Teamwork involves cooperation to achieve goals."),

                                new QD("Which leadership style involves shared decision-making?",
                                                new String[] { "Democratic leadership", "Autocratic leadership",
                                                                "Laissez-faire leadership",
                                                                "Transactional leadership" },
                                                "EASY",
                                                "Democratic leaders involve team members in decisions."),

                                new QD("Which leadership style involves full control by the leader?",
                                                new String[] { "Autocratic leadership", "Democratic leadership",
                                                                "Transformational leadership", "Servant leadership" },
                                                "EASY",
                                                "Autocratic leaders make decisions independently."),

                                new QD("What is laissez-faire leadership?",
                                                new String[] { "Minimal supervision style", "Strict control style",
                                                                "Team-focused leadership", "Directive leadership" },
                                                "MEDIUM",
                                                "Laissez-faire leaders give autonomy to team members."),

                                new QD("What is transformational leadership?",
                                                new String[] { "Inspiring change and innovation", "Maintaining routine",
                                                                "Avoiding risk", "Focusing only on rules" },
                                                "MEDIUM",
                                                "Transformational leaders motivate and inspire change."),

                                new QD("What is transactional leadership?",
                                                new String[] { "Reward-based leadership", "Vision-based leadership",
                                                                "Hands-off leadership", "Emotional leadership" },
                                                "MEDIUM",
                                                "Transactional leadership focuses on rewards and punishments."),

                                new QD("What is motivation?",
                                                new String[] { "Drive to achieve goals", "Avoiding work",
                                                                "Giving instructions", "Managing budgets" },
                                                "EASY",
                                                "Motivation energises behaviour toward goals."),

                                new QD("Which theory focuses on hierarchy of needs?",
                                                new String[] { "Maslow's hierarchy", "Herzberg theory", "Equity theory",
                                                                "Expectancy theory" },
                                                "MEDIUM",
                                                "Maslow proposed a hierarchy from basic to advanced needs."),

                                new QD("What is communication?",
                                                new String[] { "Exchange of information", "Giving orders only",
                                                                "Writing reports", "Listening only" },
                                                "EASY",
                                                "Communication involves sending and receiving messages."),

                                new QD("What is effective communication?",
                                                new String[] { "Clear and understood message exchange",
                                                                "Talking loudly", "Using technical terms",
                                                                "Sending emails" },
                                                "EASY",
                                                "Effective communication ensures understanding."),

                                new QD("What is active listening?",
                                                new String[] { "Fully concentrating on the speaker",
                                                                "Hearing without responding", "Interrupting often",
                                                                "Ignoring feedback" },
                                                "EASY",
                                                "Active listening involves attention and response."),

                                new QD("What is feedback?",
                                                new String[] { "Response to performance", "Instruction only",
                                                                "Punishment", "Reward" },
                                                "EASY",
                                                "Feedback helps improve performance."),

                                new QD("What is conflict in teams?",
                                                new String[] { "Disagreement among members", "Agreement among members",
                                                                "Team success", "Planning process" },
                                                "EASY",
                                                "Conflict arises from differing views."),

                                new QD("What is conflict resolution?",
                                                new String[] { "Resolving disagreements", "Avoiding discussions",
                                                                "Ignoring issues", "Punishing members" },
                                                "EASY",
                                                "It involves addressing and solving conflicts."),

                                new QD("What is collaboration?",
                                                new String[] { "Working together toward a goal", "Working alone",
                                                                "Competing", "Avoiding tasks" },
                                                "EASY",
                                                "Collaboration improves team outcomes."),

                                new QD("What is trust in teams?",
                                                new String[] { "Confidence in others", "Avoiding tasks",
                                                                "Controlling others", "Ignoring feedback" },
                                                "EASY",
                                                "Trust strengthens team relationships."),

                                new QD("What is team cohesion?",
                                                new String[] { "Strength of team bonds", "Individual work",
                                                                "Conflict level", "Team size" },
                                                "MEDIUM",
                                                "Cohesion reflects unity among members."),

                                new QD("What is emotional intelligence?",
                                                new String[] { "Understanding and managing emotions",
                                                                "Ignoring feelings", "Giving orders",
                                                                "Working faster" },
                                                "MEDIUM",
                                                "It helps leaders manage relationships effectively."),

                                new QD("What is self-awareness?",
                                                new String[] { "Understanding one's own emotions",
                                                                "Understanding others only", "Ignoring feedback",
                                                                "Avoiding conflict" },
                                                "MEDIUM",
                                                "Self-awareness is key to emotional intelligence."),

                                new QD("What is empathy?",
                                                new String[] { "Understanding others' feelings", "Ignoring emotions",
                                                                "Giving instructions", "Avoiding interaction" },
                                                "EASY",
                                                "Empathy builds strong relationships."),

                                new QD("What is delegation in leadership?",
                                                new String[] { "Assigning tasks to team members",
                                                                "Doing everything alone", "Avoiding work",
                                                                "Controlling strictly" },
                                                "EASY",
                                                "Delegation improves efficiency and development."),

                                new QD("What is accountability in teams?",
                                                new String[] { "Responsibility for actions", "Avoiding responsibility",
                                                                "Blaming others", "Ignoring tasks" },
                                                "EASY",
                                                "Accountability ensures ownership."),

                                new QD("What is team diversity?",
                                                new String[] { "Differences among team members", "Same background team",
                                                                "Single skill team", "Uniform ideas" },
                                                "MEDIUM",
                                                "Diversity brings varied perspectives."),

                                new QD("What is inclusion?",
                                                new String[] { "Ensuring everyone feels valued", "Ignoring differences",
                                                                "Selecting few members", "Avoiding input" },
                                                "MEDIUM",
                                                "Inclusion supports participation."),

                                new QD("What is leadership vision?",
                                                new String[] { "Clear future direction", "Daily tasks",
                                                                "Short-term goals", "Instructions" },
                                                "MEDIUM",
                                                "Vision guides long-term success."),

                                new QD("What is role clarity?",
                                                new String[] { "Understanding responsibilities", "Doing random tasks",
                                                                "Avoiding work", "Changing roles often" },
                                                "EASY",
                                                "Clear roles improve performance."),

                                new QD("What is a team role?",
                                                new String[] { "Specific responsibility in a team", "General idea",
                                                                "Random task", "Leadership style" },
                                                "EASY",
                                                "Roles define contributions."),

                                new QD("What is decision making in teams?",
                                                new String[] { "Choosing actions collectively", "Leader decides alone",
                                                                "Avoiding decisions", "Delaying tasks" },
                                                "MEDIUM",
                                                "Teams often collaborate in decisions."),

                                new QD("What is brainstorming?",
                                                new String[] { "Generating ideas freely", "Evaluating ideas",
                                                                "Rejecting ideas", "Planning tasks" },
                                                "EASY",
                                                "Brainstorming encourages creativity."),

                                new QD("What is groupthink?",
                                                new String[] { "Desire for harmony over critical thinking",
                                                                "Open debate", "Conflict resolution",
                                                                "Idea generation" },
                                                "HARD",
                                                "Groupthink can reduce decision quality."),

                                new QD("What is leadership influence?",
                                                new String[] { "Ability to affect others' behaviour", "Giving orders",
                                                                "Avoiding tasks", "Working alone" },
                                                "MEDIUM",
                                                "Influence is central to leadership."),

                                new QD("What is coaching in leadership?",
                                                new String[] { "Guiding development", "Punishing mistakes",
                                                                "Ignoring performance", "Giving orders" },
                                                "MEDIUM",
                                                "Coaching improves skills."),

                                new QD("What is mentoring?",
                                                new String[] { "Providing guidance and support", "Managing tasks",
                                                                "Assigning work", "Evaluating performance" },
                                                "MEDIUM",
                                                "Mentoring helps personal growth."),

                                new QD("What is team performance?",
                                                new String[] { "Effectiveness of team output", "Individual work",
                                                                "Task difficulty", "Meeting frequency" },
                                                "MEDIUM",
                                                "Performance measures success."),

                                new QD("What is conflict avoidance?",
                                                new String[] { "Ignoring disagreements", "Resolving conflicts",
                                                                "Encouraging debate", "Sharing ideas" },
                                                "MEDIUM",
                                                "Avoidance may delay resolution."),

                                new QD("What is constructive conflict?",
                                                new String[] { "Healthy debate improving decisions",
                                                                "Destructive arguments", "Avoiding issues",
                                                                "Ignoring opinions" },
                                                "MEDIUM",
                                                "Constructive conflict enhances outcomes."),

                                new QD("What is leadership credibility?",
                                                new String[] { "Trustworthiness of leader", "Authority level", "Power",
                                                                "Position" },
                                                "MEDIUM",
                                                "Credibility builds trust."),

                                new QD("What is team synergy?",
                                                new String[] { "Combined effort exceeds individual output",
                                                                "Working alone", "Reducing tasks", "Avoiding work" },
                                                "HARD",
                                                "Synergy creates greater results together."),

                                new QD("What is organisational communication?",
                                                new String[] { "Information flow within organisation",
                                                                "External marketing", "Hiring process", "Budgeting" },
                                                "MEDIUM",
                                                "It ensures coordination."),

                                new QD("What is leadership adaptability?",
                                                new String[] { "Adjusting style to situation", "Using one style always",
                                                                "Avoiding change", "Ignoring feedback" },
                                                "MEDIUM",
                                                "Adaptability improves effectiveness."),

                                new QD("What is situational leadership?",
                                                new String[] { "Adapting leadership style based on context",
                                                                "Strict leadership", "Hands-off approach",
                                                                "Reward-based leadership" },
                                                "HARD",
                                                "Leaders adjust based on team needs."),

                                new QD("What is team morale?",
                                                new String[] { "Overall team spirit", "Individual skill",
                                                                "Task complexity", "Leadership style only" },
                                                "EASY",
                                                "High morale boosts productivity."),

                                new QD("What is motivation theory?",
                                                new String[] { "Explains what drives behaviour", "Defines rules",
                                                                "Sets goals", "Assigns tasks" },
                                                "MEDIUM",
                                                "It studies motivation factors."),

                                new QD("What is leadership responsibility?",
                                                new String[] { "Guiding and supporting team", "Avoiding decisions",
                                                                "Delegating everything", "Working alone" },
                                                "EASY",
                                                "Leaders ensure direction and support."),

                                new QD("What is a communication barrier?",
                                                new String[] { "Obstacle to effective communication", "Clear message",
                                                                "Feedback", "Listening" },
                                                "MEDIUM",
                                                "Barriers reduce understanding."),

                                new QD("What is non-verbal communication?",
                                                new String[] { "Communication without words", "Written communication",
                                                                "Spoken communication", "Digital communication" },
                                                "EASY",
                                                "Includes body language and gestures."),

                                new QD("What is team leadership?",
                                                new String[] { "Leading a group toward goals", "Working alone",
                                                                "Managing finances", "Avoiding tasks" },
                                                "EASY",
                                                "Team leadership focuses on group success."));
        }

        // =========================================================================
        // MODULE 7: Strategy and Decision Making
        // =========================================================================

        private List<QD> strategyAndDecisionMakingQuestions() {
                return List.of(
                                new QD("What is strategy?",
                                                new String[] { "A plan to achieve long-term goals", "A daily task list",
                                                                "A financial report", "A team meeting" },
                                                "EASY",
                                                "Strategy defines how an organisation will achieve its objectives."),

                                new QD("What is decision making?",
                                                new String[] { "Choosing between alternatives", "Avoiding choices",
                                                                "Following orders", "Setting budgets" },
                                                "EASY",
                                                "Decision making involves selecting the best option among alternatives."),

                                new QD("What is strategic planning?",
                                                new String[] { "Defining long-term direction", "Managing daily tasks",
                                                                "Hiring employees", "Budgeting" },
                                                "EASY",
                                                "Strategic planning focuses on long-term goals and actions."),

                                new QD("What is a business objective?",
                                                new String[] { "A specific goal to achieve", "A random idea",
                                                                "A meeting note", "A report" },
                                                "EASY",
                                                "Objectives provide clear targets for organisations."),

                                new QD("What is a tactical decision?",
                                                new String[] { "Short-term action to support strategy",
                                                                "Long-term vision", "Random choice",
                                                                "Personal decision" },
                                                "MEDIUM",
                                                "Tactical decisions help implement strategies."),

                                new QD("What is an operational decision?",
                                                new String[] { "Day-to-day decision", "Long-term planning",
                                                                "Strategic thinking", "Vision setting" },
                                                "EASY",
                                                "Operational decisions focus on daily activities."),

                                new QD("What is SWOT analysis?",
                                                new String[] { "Evaluating strengths, weaknesses, opportunities, threats",
                                                                "Budget planning", "Hiring process", "Team building" },
                                                "EASY",
                                                "SWOT helps assess internal and external factors."),

                                new QD("What is PEST analysis?",
                                                new String[] { "Analyzing political, economic, social, technological factors",
                                                                "Hiring strategy", "Financial planning",
                                                                "Team performance" },
                                                "MEDIUM",
                                                "PEST evaluates the external environment."),

                                new QD("What is risk in decision making?",
                                                new String[] { "Possibility of loss or failure", "Guaranteed success",
                                                                "Routine action", "Planning process" },
                                                "EASY",
                                                "Risk involves uncertainty and potential negative outcomes."),

                                new QD("What is uncertainty?",
                                                new String[] { "Lack of complete information", "Full knowledge",
                                                                "Clear outcome", "Predictable situation" },
                                                "EASY",
                                                "Uncertainty makes decision making harder."),

                                new QD("What is a decision model?",
                                                new String[] { "Framework for making decisions", "A report",
                                                                "A meeting", "A policy" },
                                                "MEDIUM",
                                                "Decision models guide structured thinking."),

                                new QD("What is rational decision making?",
                                                new String[] { "Logical and structured approach", "Random choice",
                                                                "Emotional decision", "Quick guess" },
                                                "MEDIUM",
                                                "It involves systematic analysis of options."),

                                new QD("What is bounded rationality?",
                                                new String[] { "Limited decision-making ability due to constraints",
                                                                "Perfect decision making", "Unlimited knowledge",
                                                                "No decision making" },
                                                "HARD",
                                                "Humans cannot process all information perfectly."),

                                new QD("What is competitive strategy?",
                                                new String[] { "Plan to outperform competitors", "Hiring strategy",
                                                                "Budget plan", "Daily task list" },
                                                "MEDIUM",
                                                "Competitive strategy seeks market advantage."),

                                new QD("What is Porter's Five Forces?",
                                                new String[] { "Framework analyzing competitive forces",
                                                                "Financial model", "Hiring framework", "Team model" },
                                                "HARD",
                                                "It assesses industry competition."),

                                new QD("What is a strategic goal?",
                                                new String[] { "Long-term organisational aim", "Daily task",
                                                                "Random idea", "Short meeting" },
                                                "EASY",
                                                "Strategic goals guide long-term direction."),

                                new QD("What is scenario planning?",
                                                new String[] { "Preparing for multiple future situations",
                                                                "Daily scheduling", "Budgeting", "Hiring" },
                                                "HARD",
                                                "Scenario planning handles uncertainty."),

                                new QD("What is a contingency plan?",
                                                new String[] { "Backup plan for unexpected events", "Main strategy",
                                                                "Daily plan", "Budget plan" },
                                                "MEDIUM",
                                                "Contingency plans prepare for disruptions."),

                                new QD("What is a KPI?",
                                                new String[] { "Key Performance Indicator", "Key Process Input",
                                                                "Knowledge Performance Index", "Key Planning Issue" },
                                                "EASY",
                                                "KPIs measure performance against goals."),

                                new QD("What is benchmarking in strategy?",
                                                new String[] { "Comparing against best practices", "Setting budgets",
                                                                "Hiring staff", "Scheduling meetings" },
                                                "MEDIUM",
                                                "Benchmarking drives improvement."),

                                new QD("What is a business model?",
                                                new String[] { "How a company creates and delivers value",
                                                                "A financial report", "A hiring plan",
                                                                "A daily schedule" },
                                                "MEDIUM",
                                                "Business models describe value creation."),

                                new QD("What is market analysis?",
                                                new String[] { "Study of market conditions", "Employee analysis",
                                                                "Budget analysis", "IT analysis" },
                                                "MEDIUM",
                                                "Market analysis informs strategy."),

                                new QD("What is a strategic alliance?",
                                                new String[] { "Partnership between organisations",
                                                                "Competitor takeover", "Budget merger",
                                                                "Department restructure" },
                                                "MEDIUM",
                                                "Alliances combine strengths."),

                                new QD("What is corporate strategy?",
                                                new String[] { "Overall direction of the organisation",
                                                                "Department plan", "Daily operations",
                                                                "Marketing plan" },
                                                "MEDIUM",
                                                "Corporate strategy guides the whole organisation."),

                                new QD("What is a mission-driven decision?",
                                                new String[] { "Decision aligned with organisational purpose",
                                                                "Random choice", "Budget decision", "Hiring decision" },
                                                "MEDIUM",
                                                "Mission-driven decisions reflect core values."),

                                new QD("What is stakeholder analysis?",
                                                new String[] { "Identifying those affected by decisions",
                                                                "Budget planning", "Market research",
                                                                "Hiring process" },
                                                "MEDIUM",
                                                "Stakeholder analysis informs strategy."),

                                new QD("What is decision matrix?",
                                                new String[] { "Tool for evaluating options", "Financial report",
                                                                "Hiring tool", "Budget template" },
                                                "HARD",
                                                "Decision matrices compare alternatives."),

                                new QD("What is group decision making?",
                                                new String[] { "Decisions made collectively", "One person decides",
                                                                "Random process", "Automated process" },
                                                "EASY",
                                                "Groups bring diverse perspectives."),

                                new QD("What is intuitive decision making?",
                                                new String[] { "Based on instinct and experience",
                                                                "Data-driven decision", "Random choice",
                                                                "No decision" },
                                                "MEDIUM",
                                                "Intuition draws on past experience."),

                                new QD("What is data-driven decision making?",
                                                new String[] { "Using data to guide decisions", "Guessing",
                                                                "Following tradition", "Avoiding analysis" },
                                                "MEDIUM",
                                                "Data reduces bias in decisions."),

                                new QD("What is opportunity cost?",
                                                new String[] { "Value of next best alternative forgone", "Total cost",
                                                                "Profit margin", "Break-even point" },
                                                "HARD",
                                                "Every decision has a trade-off."),

                                new QD("What is a cost-benefit analysis?",
                                                new String[] { "Comparing costs and benefits of a decision",
                                                                "Counting employees", "Scheduling tasks",
                                                                "Reviewing policies" },
                                                "MEDIUM",
                                                "It helps evaluate if a decision is worthwhile."),

                                new QD("What is strategic implementation?",
                                                new String[] { "Putting strategy into action", "Planning strategy",
                                                                "Reviewing strategy", "Deleting strategy" },
                                                "MEDIUM",
                                                "Implementation turns plans into results."),

                                new QD("What is strategic evaluation?",
                                                new String[] { "Reviewing strategy effectiveness", "Creating strategy",
                                                                "Hiring for strategy", "Budgeting strategy" },
                                                "MEDIUM",
                                                "Evaluation ensures strategy works."),

                                new QD("What is the BCG matrix?",
                                                new String[] { "Tool for managing business portfolio",
                                                                "Financial statement", "Hiring model",
                                                                "Conflict tool" },
                                                "HARD",
                                                "BCG classifies products by growth and share."),

                                new QD("What is a decision tree?",
                                                new String[] { "Visual tool mapping decision options",
                                                                "Financial chart", "Organisational chart",
                                                                "Progress chart" },
                                                "MEDIUM",
                                                "Decision trees map choices and outcomes."),

                                new QD("What is strategic leadership?",
                                                new String[] { "Leading with long-term direction",
                                                                "Managing daily tasks", "Avoiding decisions",
                                                                "Following trends" },
                                                "MEDIUM",
                                                "Strategic leaders shape organisational future."),

                                new QD("What is core competency?",
                                                new String[] { "Unique strength of an organisation", "Daily task",
                                                                "Budget item", "Hiring skill" },
                                                "MEDIUM",
                                                "Core competencies provide competitive advantage."),

                                new QD("What is sustainable competitive advantage?",
                                                new String[] { "Long-lasting edge over competitors", "Short-term gain",
                                                                "Price reduction", "Staff increase" },
                                                "HARD",
                                                "Sustainability ensures long-term success."),

                                new QD("What is innovation strategy?",
                                                new String[] { "Plan for developing new ideas", "Cost reduction plan",
                                                                "Hiring plan", "Daily schedule" },
                                                "MEDIUM",
                                                "Innovation drives growth and differentiation."),

                                new QD("What is a resource-based view?",
                                                new String[] { "Strategy based on internal resources",
                                                                "Market-focused strategy", "Customer-focused strategy",
                                                                "Technology-focused strategy" },
                                                "HARD",
                                                "RBV emphasises unique internal strengths."),

                                new QD("What is strategic thinking?",
                                                new String[] { "Long-term goal-oriented thinking",
                                                                "Daily task management", "Random thinking",
                                                                "Short-term planning" },
                                                "MEDIUM",
                                                "Strategic thinking drives planning."),

                                new QD("What is a value chain?",
                                                new String[] { "Activities creating value for customers",
                                                                "Financial chain", "Supply list", "Employee list" },
                                                "HARD",
                                                "Value chain analysis identifies strengths."),

                                new QD("What is organisational agility?",
                                                new String[] { "Ability to adapt quickly to change", "Working slowly",
                                                                "Following strict rules", "Avoiding decisions" },
                                                "MEDIUM",
                                                "Agility helps survive disruption."),

                                new QD("What is a strategic pivot?",
                                                new String[] { "Changing direction based on new information",
                                                                "Keeping same strategy", "Hiring new staff",
                                                                "Cutting budget" },
                                                "HARD",
                                                "Pivots respond to market changes."),

                                new QD("What is execution risk?",
                                                new String[] { "Risk of failing to implement strategy", "Planning risk",
                                                                "Hiring risk", "Budget risk" },
                                                "HARD",
                                                "Poor execution undermines good strategy."),

                                new QD("What is strategic alignment?",
                                                new String[] { "Ensuring all activities support strategy",
                                                                "Budget alignment", "Hiring alignment",
                                                                "Schedule alignment" },
                                                "MEDIUM",
                                                "Alignment ensures coherent direction."),

                                new QD("What is a growth strategy?",
                                                new String[] { "Plan to expand the organisation", "Cost-cutting plan",
                                                                "Downsizing plan", "Maintenance plan" },
                                                "EASY",
                                                "Growth strategies increase scale or reach."),

                                new QD("What is a retrenchment strategy?",
                                                new String[] { "Cutting back to improve performance",
                                                                "Growing the business", "Hiring more staff",
                                                                "Expanding markets" },
                                                "MEDIUM",
                                                "Retrenchment reduces costs or scope."),

                                new QD("What is strategic vision?",
                                                new String[] { "Inspiring future direction", "Daily task list",
                                                                "Budget overview", "Hiring plan" },
                                                "EASY",
                                                "Vision motivates long-term action."));
        }
}
