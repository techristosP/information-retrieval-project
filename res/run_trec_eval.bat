@echo off
setlocal enabledelayedexpansion

set trec_eval=trec_eval.exe
set qrels=qrels.txt
set results_pattern=results-*.txt

for %%f in (%results_pattern%) do (
    echo Evaluating file: %%f
    set results=%%f
    set output=trec-%%~nf.eval
    !trec_eval! %qrels% !results! > !output!
    echo Processed and saved output to %%~nf.eval
    echo ---------------------------------------------------
)

echo All evaluations are completed.
pause