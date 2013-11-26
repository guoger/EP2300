#!/usr/bin/python
'''
Title: create plots
Author: Niklas Semmler
'''
import matplotlib.pyplot as plt
import numpy as np
import argparse
from scipy.stats import gaussian_kde

WARM_UP_PERIOD = 60
PLOT_PATH = "plots"
DEFAULT_PATH = "EP2300-Makeup-CodeTemplate/sim-results/out/result.log"
NUM_PDF_VALUES = 20
NAMES_LIST = ['index', 'actMax', 'estMax', 'estMaxErr', 'actAvg', 'estAvg',
    'estAvgErr', 'maxMsgRate', 'overhead']
colors = {'actMax':'rx', 'estMax':'yo', 'estMaxErr':'gs', 'actAvg':'y*',
    'estAvg':'mp', 'estAvgErr':'cd', 'maxMsgRate':'b+' }
names = {}
i = 0

for name in NAMES_LIST:
    names[name] = i
    i += 1

def time_series_plot(ax, data, variables):
    ax.grid(True)
    time = data['index']
    for var in variables:
        plt.plot(time, data[var], colors[var], label=var)
    plt.xlabel("time")
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, labels, numpoints=1)

def trade_off_plot(ax, data, variables):
    ax.grid(True)
    overhead = data['overhead']
    for var in variables:
        plt.plot(data[var], overhead, colors[var], label=var)
    plt.ylabel("overhead")
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, labels, numpoints=1)

def density_plot(ax, data, variables):
    ax.grid(True)
    for var in variables:
        data_wo_warmup = data[var][WARM_UP_PERIOD:]
        density = gaussian_kde(data_wo_warmup)
        rangex = np.arange(min(data_wo_warmup), max(data_wo_warmup), NUM_PDF_VALUES)
        plt.plot(rangex, density(rangex), colors[var], label=var)
    plt.ylabel("pdf")
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, labels, numpoints=1)

def parse_args(options):
    parser = argparse.ArgumentParser()
    for option in options:
        parser.add_argument(option[0], **option[1])
    return parser.parse_args()

def read_data(input_file):
    data = np.genfromtxt(input_file, delimiter=' ')
    data2 = {}
    data2['index'] = data[:, names['index']]
    data2['actMax'] = data[:, names['actMax']]
    data2['actAvg'] = data[:, names['actAvg']]
    data2['overhead'] = data[:, names['overhead']]
    data2['estMaxErr'] = data[:, names['estMaxErr']]
    data2['estAvgErr'] = data[:, names['estAvgErr']]
    data2['estMax'] = data[:, names['estMax']]
    data2['estAvg'] = data[:, names['estAvg']]
    return data2

def draw_plots(data):
    fig = plt.figure(1)
    fig.suptitle("time series")
    ax = fig.add_subplot(311)
    time_series_plot(ax, data,
        ["actMax", "estMax"]
    )
    ax = fig.add_subplot(312)
    time_series_plot(ax, data,
        ["actAvg", "estAvg"]
    )
    ax = fig.add_subplot(313)
    time_series_plot(ax, data,
        ["actMax", "estMax", "actAvg", "estAvg"]
    )
    fig.savefig(os.path.join(PLOT_PATH, "time_series.png"))

    fig = plt.figure(2)
    fig.suptitle("trade off")
    ax = fig.add_subplot(211)
    trade_off_plot(ax, data,
        ["estMaxErr"]
    )
    ax = fig.add_subplot(212)
    trade_off_plot(ax, data,
        ["estAvgErr"]
    )
    fig.savefig(os.path.join(PLOT_PATH, "trade_off.png"))

    fig = plt.figure(3)
    fig.suptitle("density")
    ax = fig.add_subplot(221)
    density_plot(ax, data,
        ["estMaxErr" ]
    )
    ax = fig.add_subplot(222)
    density_plot(ax, data,
        ["estAvgErr"]
    )
    fig.savefig(os.path.join(PLOT_PATH, "pdf_of_error.png"))

def main():
    options = [
        ('-f', {'dest':"input_file", 'type':str, 'required':False,
            'help':"locate the log file"}),
        ('-s', {'action':"store_true", 'dest':"only_save",
            'help':"set if you want to save instead of show"}),
    ]
    args = parse_args(options)
    data = read_data(args.input_file or DEFAULT_PATH)
    draw_plots(data)

    if not args.only_save:
        plt.show()

if __name__ == "__main__":
    path = "/Users/pharaoh/Downloads/first_valid_result.log"
    main()

"""
    TODO: add subplot titles
    TODO: reorder plots for clear comparison of R_1 and R_2
"""
