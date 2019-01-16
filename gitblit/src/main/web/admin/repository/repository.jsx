import React from "react";
import PropTypes from "prop-types";
import {Breadcrumb, Button, Dropdown, Layout, Table} from "element-react";
import {Link} from "react-router-dom";
import {library} from "@fortawesome/fontawesome-svg-core";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFile, faPlus} from "@fortawesome/free-solid-svg-icons";

import CreateFile from "./create-file";

library.add(faPlus);
library.add(faFile);

export default class Repository extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            repositoryName: props.match.params.repositoryName,
            branch: "master",
            creating: false,
            createType: null,
            columns: [
                {
                    label: "",
                    width: "80px",
                    render: (data) => {
                        if (data.isTree && !data.isFile) {
                            return <FontAwesomeIcon icon="folder"/>;
                        }
                        return <FontAwesomeIcon icon="file"/>;
                    }
                },
                {
                    label: "Name",
                    render: (data) => {
                        if (data.isFile) {
                            return <Link to={{
                                pathName: `/editor/${data.path}`,
                                state: {
                                    repositoryName: this.state.repositoryName,
                                    branch: this.state.branch
                                }
                            }}>{data.name}</Link>;
                        }
                        return data.name;
                    }
                },
                {
                    label: "Date",
                    prop: "date"
                }
            ],
            data: [{
                date: "2019-05-02",
                name: "Fold 1",
                path: "f1",
                isTree: true,
                isFile: false
            }, {
                date: "2019-05-04",
                name: "Fold 2",
                path: "f2",
                isTree: true,
                isFile: false
            }, {
                date: "2019-05-01",
                name: "File 1",
                path: "f1/s1",
                isTree: false,
                isFile: true
            }, {
                date: "2019-05-03",
                name: "File 2",
                path: "f1/s2",
                isTree: false,
                isFile: true
            }]
        };
    }

    componentDidMount() {
        this.reload();
    }

    reload() {
        fetch(`/api/repository/tree/${this.state.repositoryName}/${this.state.branch}/`).then((data) => {
            this.setState({data});
        });
    }

    createFile(createType) {
        this.setState({
            creating: true,
            createType
        });
    }

    onCreate() {
        this.setState({creating: false});
        this.reload();
    }

    onCancel() {
        this.setState({creating: false});
    }

    render() {
        return (
            <div>
                <Layout.Row>
                    <Layout.Col span="16">
                        <Breadcrumb separator="/">
                            <Breadcrumb.Item><Link to="/">Home</Link></Breadcrumb.Item>
                            <Breadcrumb.Item><Link to="/project">Project</Link></Breadcrumb.Item>
                            <Breadcrumb.Item>Project {this.state.id}</Breadcrumb.Item>
                        </Breadcrumb>
                    </Layout.Col>
                    <Layout.Col span="8">
                        <div className="head-operation">
                            <Dropdown menu={(
                                <Dropdown.Menu>
                                    <Dropdown.Item>
                                        <span onClick={() => this.createFile("file")}>Create File</span>
                                    </Dropdown.Item>
                                    <Dropdown.Item>
                                        <span onClick={() => this.createFile("folder")}>Create Folder</span>
                                    </Dropdown.Item>
                                </Dropdown.Menu>
                            )}>
                                <Button size="small">Create </Button>
                            </Dropdown>
                        </div>
                    </Layout.Col>
                </Layout.Row>
                <Layout.Row>
                    <Layout.Col span="24">
                        <Table
                            style={{width: "100%"}}
                            columns={this.state.columns}
                            data={this.state.data}
                            stripe={true}
                        />
                    </Layout.Col>
                </Layout.Row>
                {this.state.creating &&
                <CreateFile type={this.state.createType} onCreate={() => this.onCreate()} onCancel={() => this.onCancel()}/>
                }
            </div>
        );
    }

}

Repository.propTypes = {match: PropTypes.object};
