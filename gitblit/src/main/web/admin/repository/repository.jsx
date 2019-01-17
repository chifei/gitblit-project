import React from "react";
import PropTypes from "prop-types";
import {Breadcrumb, Button, Layout, Message as notify, Table} from "element-react";
import {Link} from "react-router-dom";
import {library} from "@fortawesome/fontawesome-svg-core";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFile, faFolder, faPlus, faReply} from "@fortawesome/free-solid-svg-icons";

import CreateFile from "./create-file";

library.add(faPlus);
library.add(faFile);
library.add(faFolder);
library.add(faReply);

export default class Repository extends React.Component {
    constructor(props) {
        super(props);
        const path = props.location.pathname.replace(`/console/repo/${props.match.params.repositoryName}`, "");
        this.state = {
            repositoryName: props.match.params.repositoryName,
            path: path.indexOf("/") === 0 ? path.replace("/", "") : path,
            branch: "master",
            createFileEnabled: false,
            createFolderEnabled: false,
            changed: false,
            columns: [
                {
                    label: "",
                    width: "80px",
                    render: (data) => {
                        if (data.isTree && !data.isFile) {
                            return <FontAwesomeIcon icon="folder"/>;
                        } else if (data.isToUp) {
                            return <a href="#" onClick={() => this.changeFolder(data.path)}><FontAwesomeIcon icon="reply"/>...</a>;
                        }
                        return <FontAwesomeIcon icon="file"/>;
                    }
                },
                {
                    label: "Name",
                    render: (data) => {
                        if (data.isFile) {
                            return <Link to={{
                                pathname: `/console/editor/${data.path}`,
                                state: {
                                    repositoryName: this.state.repositoryName,
                                    branch: this.state.branch,
                                    name: data.name
                                }
                            }}>{data.name}</Link>;
                        }
                        return <a href="#" onClick={() => this.changeFolder(data.path)}>{data.name}</a>;
                    }
                },
                {
                    label: "Date",
                    prop: "date"
                }
            ],
            data: []
        };
    }

    componentDidMount() {
        if (this.state.path) {
            this.changeFolder(this.state.path);
        }
        this.reload();

        fetch(`/api/repository/status/${this.state.repositoryName}/master`).then((response) => {
            this.setState({changed: response.changed});
        });
    }

    reload() {
        let url = `/api/repository/tree/${this.state.repositoryName}/${this.state.branch}`;
        if (this.state.path) {
            url += "/" + this.state.path;
        }
        fetch(url).then((response) => {
            let data = [];
            if (this.state.path) {
                data.push({
                    name: null,
                    path: this.state.path.substr(0, this.state.path.lastIndexOf("/")),
                    isToUp: true
                });
            }
            data = data.concat(response);
            this.setState({data});
        });
    }

    createFile() {
        this.setState({createFileEnabled: true});
    }

    createFolder() {
        this.setState({createFolderEnabled: true});
    }

    onCreate(name) {
        this.props.history.push({
            pathname: `/console/editor/${name}`,
            state: {
                name: name,
                repositoryName: this.state.repositoryName,
                branch: "master",
                isNew: true
            }
        });
    }

    onCreateFolder(name) {
        fetch(`/api/repository/folder/${this.state.repositoryName}/${this.state.branch}/${name}`, {method: "POST"}).then(() => {
            this.setState({createFolderEnabled: false});
            this.reload();
        });
    }

    changeFolder(path) {
        const paths = path.split("/");
        let newPath = "";
        const folders = [];
        paths.forEach((p) => {
            if (p) {
                if (newPath) {
                    newPath += "/";
                }
                newPath += p;
                folders.push(newPath);
            }
        });
        this.setState({
            path,
            folders
        }, () => {
            this.props.history.push(`/console/repo/${this.state.repositoryName}/${path}`);
            this.reload();
            notify({
                message: "Folder Created",
                type: "info"
            });
        });

    }

    commit() {
        fetch(`/api/repository/commit/${this.state.repositoryName}/master`).then((response) => {
            this.setState({changed: false});
            this.reload();
            notify({
                message: "Commit Success",
                type: "success"
            });
        });
    }

    revert() {
        fetch(`/api/repository/revert/${this.state.repositoryName}/master`).then((response) => {
            this.setState({changed: false});
            this.reload();
            notify({
                message: "Revert Success",
                type: "info"
            });
        });
    }

    onCancel() {
        this.setState({
            createFileEnabled: false,
            createFolderEnabled: false
        });
    }

    render() {
        return (
            <div>
                {
                    this.state.changed &&
                    <div className="repository__buttons">
                        <Button onClick={() => this.commit()}>Commit</Button>
                        <Button onClick={() => this.revert()}>Revert</Button>
                    </div>
                }

                <Layout.Row>
                    <Layout.Col span="16">
                        <Breadcrumb separator="/">
                            <Breadcrumb.Item><Link to="/console">Home</Link></Breadcrumb.Item>
                            <Breadcrumb.Item>
                                <a href="#" onClick={() => this.changeFolder("")}>{this.state.repositoryName}</a>
                            </Breadcrumb.Item>
                            {this.state.folders && this.state.folders.map(p =>
                                <Breadcrumb.Item key={p}><a href="#" onClick={() => this.changeFolder(p)}>{p.split("/")[p.split("/").length - 1]}</a></Breadcrumb.Item>
                            )}

                        </Breadcrumb>
                    </Layout.Col>
                    <Layout.Col span="8">
                        <div className="head-operation">
                            <Button size="small" onClick={() => this.createFile()}>New File</Button>
                            <Button size="small" onClick={() => this.createFolder()}>New Folder</Button>
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
                {this.state.createFileEnabled &&
                <CreateFile name={this.state.path ? this.state.path + "/" : null} onCreate={value => this.onCreate(value)} onCancel={() => this.onCancel()}/>
                }
                {this.state.createFolderEnabled &&
                <CreateFile name={this.state.path ? this.state.path + "/" : null} onCreate={value => this.onCreateFolder(value)} onCancel={() => this.onCancel()}/>
                }
            </div>
        );
    }

}

Repository.propTypes = {
    history: PropTypes.object,
    location: PropTypes.object,
    match: PropTypes.object
};
